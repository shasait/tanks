/*
 * Copyright (C) 2017 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.tanks.app.common.model;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import de.hasait.tanks.app.common.msg.UpdateMsg;

/**
 *
 */
public class DistributedModel implements Disposable {

	private final Set<Address> _channelMembers = new HashSet<>();

	private final AtomicReference<JChannel> _channel = new AtomicReference<>();
	private final AtomicReference<Model> _model = new AtomicReference<>();

	public DistributedModel() {
		super();
	}

	public void connect(final String pRoomName, final int pWishPiecesX, final int pWishPiecesY) {
		if (_channel.get() != null) {
			throw new IllegalStateException("Already connected");
		}
		final JChannel channel;
		try {
			channel = new JChannel();
		} catch (Exception pE) {
			throw new RuntimeException("Could not create JChannel", pE);
		}
		if (!_channel.compareAndSet(null, channel)) {
			throw new IllegalStateException("Already connected");
		}
		try {
			channel.setReceiver(new ReceiverAdapter() {
				@Override
				public void getState(final OutputStream pOutput) throws Exception {
					try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(pOutput, 1024))) {
						oos.writeInt(getModel().getPiecesX());
						oos.writeInt(getModel().getPiecesY());
						oos.writeObject(getModel().getSharedState());
					}
				}

				@Override
				public void receive(final Message pMessage) {
					final Object received = pMessage.getObject();
					networkReceive(received);
				}

				@Override
				public void setState(final InputStream pInput) throws Exception {
					try (final ObjectInputStream ois = new ObjectInputStream(pInput)) {
						initModel(new Model(ois.readInt(), ois.readInt()));
						((Iterable<Object>) ois.readObject()).forEach(DistributedModel.this::networkReceive);
					}
				}

				@Override
				public void viewAccepted(final View pView) {
					final List<Address> members = pView.getMembers();
					if (!hasModel() && members.size() == 1) {
						initModel(new Model(pWishPiecesX, pWishPiecesY));
					}
					final Iterator<Address> entryI = _channelMembers.iterator();
					while (entryI.hasNext()) {
						final Address address = entryI.next();
						if (!members.contains(address)) {
							final String addressString = address.toString();
							entryI.remove();
							final Predicate<AbstractGameObject<?>> addressStringPredicate = pObject -> pObject.getOwnerAddress()
																											  .equals(addressString);
							getModel().removeTanks(addressStringPredicate);
							getModel().removeBullets(addressStringPredicate);
						}
					}
					_channelMembers.addAll(members);
				}

				private void initModel(final Model pModel) {
					if (!_model.compareAndSet(null, pModel)) {
						throw new IllegalStateException("Model exists!?");
					}
				}
			});
			channel.connect(pRoomName);
			channel.getState(null, 0);
		} catch (Exception pE) {
			throw new RuntimeException(pE);
		}
	}

	public void createBullet(final Tank pTank) {
		final JChannel channel = getChannelNotNull();
		final TankState state = pTank.getState();
		final Bullet bullet = new Bullet(channel.getAddressAsString(), pTank.getUuid(), state._centerX, state._centerY,
										 state._rotation + state._turretRotation
		);
		networkSend(bullet);
	}

	public void createTank(final PlayerConfig pPlayerConfig) {
		final JChannel channel = getChannelNotNull();
		final Tank tank = new Tank(channel.getAddressAsString(), pPlayerConfig.getName(), getModel().getTankW(), getModel().getTankH(),
								   TimeUtils.millis() + getModel().getRules()._spawnTimeMillis
		);
		final LocalTank localTank = new LocalTank(tank.getUuid(), pPlayerConfig);
		getModel().addLocalTank(localTank);
		networkSend(tank);
	}

	@Override
	public void dispose() {
		final JChannel channel = _channel.getAndSet(null);
		if (channel != null) {
			channel.close();
		}
		_model.getAndSet(null);
	}

	public Model getModel() {
		final Model model = _model.get();
		if (model == null) {
			throw new IllegalStateException("No model");
		}
		return model;
	}

	public boolean hasModel() {
		return _model.get() != null;
	}

	public void networkSend(final Object pObject) {
		final JChannel channel = getChannelNotNull();
		try {
			channel.send(new Message(null, pObject));
		} catch (final Exception pE) {
			throw new RuntimeException(pE);
		}
	}

	private JChannel getChannelNotNull() {
		final JChannel channel = _channel.get();
		if (channel == null) {
			throw new IllegalStateException("Not connected");
		}
		return channel;
	}

	private void networkReceive(final Object pReceived) {
		if (pReceived instanceof UpdateMsg) {
			final UpdateMsg dirty = (UpdateMsg) pReceived;
			for (final TankState tankState : dirty._tanks) {
				getModel().apply(tankState);
			}
			for (final BulletState bulletState : dirty._bullets) {
				getModel().apply(bulletState);
			}
			for (final String bulletUuid : dirty._removedBullets) {
				getModel().removeBullet(bulletUuid);
			}
			for (final String tankUuid : dirty._incrementDamage) {
				getModel().getLocalTank(tankUuid).ifPresent(LocalTank::incrementDamageIncrement);
			}
		}
		if (pReceived instanceof Tank) {
			final Tank tank = (Tank) pReceived;
			getModel().addTank(tank);
		}
		if (pReceived instanceof Bullet) {
			final Bullet bullet = (Bullet) pReceived;
			getModel().addBullet(bullet);
		}
		if (pReceived instanceof Rules) {
			final Rules rules = (Rules) pReceived;
			getModel().setRules(rules);
		}
	}


}

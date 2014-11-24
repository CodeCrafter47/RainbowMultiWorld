package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Shadow;
import WrapperObjects.Entities.EntityWrapper;
import codecrafter47.multiworld.api.ChatPlayer;
import joebkt.EntityGeneric;
import joebkt.EntityPlayer;
import joebkt.JSonConverter;
import joebkt.TextObject;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Created by florian on 23.11.14.
 */
@Mixin(WrapperObjects.Entities.PlayerWrapper.class)
public class ImplementChatAPI extends EntityWrapper implements ChatPlayer {

	@Shadow
	public EntityPlayer plr;

	public ImplementChatAPI(EntityGeneric argEnt) {
		super(argEnt);
	}

	@Override public void sendMessage(BaseComponent... components) {
		TextObject var7 = JSonConverter.getTextObjectFromString(ComponentSerializer.toString(components));
		plr.sendMessageObj(var7);
	}
}

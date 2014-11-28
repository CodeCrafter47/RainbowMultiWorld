package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import joebkt.*;

/**
 * Created by florian on 28.11.14.
 */
@Mixin(joebkt.CmdGameRule.class)
public abstract class FixCmdGamerule extends CommandAbstract {

	@Shadow
	abstract GameRules d();

	@Overwrite
	public void handleCommand(CommandSender commandSender, String[] var2) throws di_BaseException {
		GameRules var3 = this.d();
		if(commandSender instanceof EntityPlayer){
			var3 = ((EntityPlayer)commandSender).getWorldServer().getGameRules();
		}
		String var4 = var2.length > 0?var2[0]:"";
		String var5 = var2.length > 1?a(var2, 1):"";
		switch(var2.length) {
			case 0:
				commandSender.sendMessageObj(new TextComponent(a(var3.b())));
				break;
			case 1:
				if(!var3.e(var4)) {
					throw new di_BaseException("commands.gamerule.norule", new Object[]{var4});
				}

				String var6 = var3.a(var4);
				commandSender.sendMessageObj((new TextComponent(var4)).a(" = ").a(var6));
				commandSender.a(CommandResultTypeMaybe.QUERY_RESULT, var3.getGameRuleByName(var4));
				break;
			default:
				if(var3.a(var4, EnumAnyBoolOrNumeric.BOOLEAN_VALUE) && !"true".equals(var5) && !"false".equals(var5)) {
					throw new di_BaseException("commands.generic.boolean.invalid", new Object[]{var5});
				}

				var3.a(var4, var5);
				CmdGameRule.a(var3, var4);
				a(commandSender, this, "commands.gamerule.success", new Object[0]);
		}

	}
}

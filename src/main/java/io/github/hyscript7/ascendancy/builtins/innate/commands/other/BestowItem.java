package io.github.hyscript7.ascendancy.builtins.innate.commands.other;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestowItem extends AbstractInnateCommand {

    public BestowItem() {
        super("bestow item", "Item Bestowal", false, false, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        int count = Integer.parseInt(context.args()[0]);
        String thing = context.args()[1];

        Material material = Material.getMaterial(thing.replace(" ", "_").toUpperCase());
        if (material == null || context.invoker().getInventory().contains(material)) {
            ItemStack theThing = null;
            int actualCount = 0;
            for (ItemStack item : context.invoker().getInventory().getContents()) {
                if (item != null) {
                    actualCount = Math.min(item.getAmount(), count);
                    theThing = item.clone();
                    theThing.setAmount(actualCount);
                    context.invoker().getInventory().remove(theThing);
                    break;
                }
            }
            if (theThing != null) {
                context.target().getInventory().addItem(theThing);
                String itemAndCount = actualCount + " " + thing.toLowerCase();
                AscendancyMessagingAPI.getInstance().send(context.target(), AscendancyMessagingAPI.MessageType.INFO, "You have been bestowed" + itemAndCount +" by " + context.invoker().getName() + "!");
                AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.SUCCESS, "You have bestowed " + context.target().getName() + itemAndCount + "!");
            } else {
                AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "You do not have any " + thing.toLowerCase() + "!");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(message).matches(".+ I bestow you (the|\\d+)? .+ (item)?(s)?");
    }

    private static final Pattern argsPattern = Pattern.compile("(?<count>the|\\d+)? (?<thing>.+)(s)? (item)?(s)?");

    @Override
    public String[] resolveArguments(String message) {
        String pureArgs = InnateUtils.unifyInnateCommand(InnateUtils.removeNonAlpha(message, true)).split("I bestow you")[1].trim();
        Matcher m = argsPattern.matcher(pureArgs.trim());
        m.matches(); // Since this won't be called unless the matchesMessage() passes, we should be good.
        String countGroup = m.group("count");
        if (countGroup == null || countGroup.isEmpty() || countGroup.equalsIgnoreCase("the")) {
            countGroup = "1";
        }
        String thing = m.group("thing");
        return new String[]{countGroup, thing};
    }

    @Override
    public String getUsage() {
        return "<name> I bestow you (the | [1|2|...|64]) <item name> [item(s)]";
    }
}

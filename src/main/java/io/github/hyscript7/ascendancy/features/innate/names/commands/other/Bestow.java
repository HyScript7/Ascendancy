package io.github.hyscript7.ascendancy.features.innate.names.commands.other;

import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bestow extends AbstractInnateCommand {

    public Bestow() {
        super("bestow", "Bestowal", false, false, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        int count = Integer.parseInt(context.args()[0]);
        String thing = context.args()[1];
        String type = context.args()[2];

        if (type.equals("item")) {
            Material material = Material.getMaterial(thing.replace(" ", "_").toUpperCase());
            boolean matchByName = material == null;
            if (material == null || context.invoker().getInventory().contains(material)) {
                ItemStack theThing = null;
                int actualCount = 0;
                for (ItemStack item : context.invoker().getInventory().getContents()) {
                    if (item != null && ((matchByName) || (material != null && item.getType().equals(material)))) {
                        if (matchByName) {
                            if (!item.hasItemMeta() || InnateUtils.removeNonAlpha(((TextComponent) item.getItemMeta().itemName()).content()).equalsIgnoreCase(thing)) {
                                continue;
                            }
                        }
                        actualCount = Math.min(item.getAmount(), count);
                        theThing = item.clone();
                        theThing.setAmount(actualCount);
                        context.invoker().getInventory().remove(theThing);
                        break;
                    }
                }
                if (theThing != null) {
                    context.target().getInventory().addItem(theThing);
                }
            }
        }

        return true;
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(message).matches(".+ I bestow you (the|\\d+)? .+ (item)?");
    }

    private static final Pattern argsPattern = Pattern.compile("(?<count>the|\\d+)? (?<thing>.+)(s)? (?<type>item)?(s)?");

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
        String type = m.group("type");
        if (type == null || type.isEmpty()) {
            type = "item";
        }
        return new String[]{countGroup, thing, type};
    }

    @Override
    public String getUsage() {
        return "<name> I bestow you (the | [1|2|...|64]) <item name> [item(s)]";
    }
}

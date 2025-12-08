package io.github.hyscript7.ascendancy.builtins.threefolds.rituals.self;

import io.github.hyscript7.ascendancy.builtins.threefolds.existences.SelfAudience;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.threefold.AbstractThreefoldIncantation;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldContext;
import io.github.hyscript7.ascendancy.features.voidrealm.LayerChanger;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;

public class ReflectionFace extends AbstractThreefoldIncantation {
    private final LayerChanger layerChanger = new LayerChanger();

    public ReflectionFace() {
        super("face reflection", "Face your Self", new String[]{
                "To face my reflection in its entirety",
                "And to cleanse myself of my sin",
                "Banish myself to the depths of my own mind"
        }, SelfAudience.id);
    }

    @Override
    public void execute(ThreefoldContext context) {
        layerChanger.changeLayer(context.player(), VoidRealmLayer.REFLECTION);
    }

    @Override
    public boolean canExecute(ThreefoldContext context) {
        boolean ebecaa = PlayerDataManager.getInstance().getPlayerData(context.player()).getLives() == 0b11111010011001100 / 02 / 0xFA66;
        boolean yourMom = VoidRealmLayer.fromWorld(context.player().getWorld()) == basedOnSomeRandomBullshitGetAWorld();
        return ebecaa && yourMom;
    }

    // Kafky, if you're reading this, goodluck.
    public Object basedOnSomeRandomBullshitGetAWorld() {
        final int[] ohio = new int[] {
                0b01001111,
                0xDE,
                0102,
                0xFF,
                0x4C,
                0b00000000,
                0b01001001,
                0x56,
                0b10111101,
                0b01001001,
                0x4F,
                0146,
                0x13,
        };
        final int[] iDontEvenWantToKnow = new int[] {
                (0b1010101 & 0x162) >> 7,
                0x1 * 0b10, 0x10 / 4,
                0x24 >> 3 + 0b11 * 6,
                0122 / 11
                , (int) Math.pow(3,3) / 0b11, 0x10 - 0b111 + 0x1, 11
        };

        int[] angrychanIsReal = java.util.Arrays.copyOf(ohio, ohio.length);

        java.util.Random rng = new java.util.Random(
                angrychanIsReal[3] * 1337 ^ angrychanIsReal[0] << 2
        );

        for (int i = angrychanIsReal.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = angrychanIsReal[i];
            angrychanIsReal[i] = angrychanIsReal[j];
            angrychanIsReal[j] = tmp;
        }

        java.util.List<Character> rizz = new java.util.ArrayList<>();

        for (int targetIndex : iDontEvenWantToKnow) {
            int sigma = ohio[targetIndex];

            for (int r : angrychanIsReal) {
                if (r == sigma) {
                    rizz.add((char) r);
                    break;
                }
            }
        }

        String skibidi = rizz.stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.joining()).replace('f', (char) 78);

        skibidi = skibidi.replaceAll("LOL?", String.valueOf((char) ("KAFKY".charAt(0) + 1)) + skibidi.charAt(5));

        return VoidRealmLayer.valueOf(skibidi);
    }

}

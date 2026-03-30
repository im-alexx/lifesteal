package dev.lifesteal.lifesteal.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MeteorDetectedScreen extends Screen {
    private static final Text TITLE = Text.literal("Game crashed... almost");
    private final Text[] messageLines;

    public MeteorDetectedScreen(String detectedClientName) {
        super(TITLE);
        this.messageLines = new Text[] {
                Text.literal("Whoops!").formatted(Formatting.RED),
                Text.empty()
                        .append(Text.literal("Lifesteal Mod").formatted(Formatting.GREEN))
                        .append(Text.literal(" detected you using hacks, specifically "))
                        .append(Text.literal(detectedClientName).formatted(Formatting.RED))
                        .append(Text.literal(".")),
                Text.literal("Using hacks is a big no-no! It's also against The Minecraft EULA and Community Standards!"),
                Text.literal("I'm gonna have to ask you to quickly go and delete that, kindly.")
        };
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int buttonX = this.width / 2 - buttonWidth / 2;
        int buttonY = this.height / 2 + 52;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close Game"), button -> {
            if (this.client != null) {
                this.client.scheduleStop();
            }
        }).dimensions(buttonX, buttonY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 85, -1);
        int lineY = this.height / 2 - 60;
        for (Text line : messageLines) {
            context.drawCenteredTextWithShadow(this.textRenderer, line, this.width / 2, lineY, -1);
            lineY += 14;
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

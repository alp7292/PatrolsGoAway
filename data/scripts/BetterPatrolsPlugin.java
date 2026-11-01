package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class BetterPatrolsPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        if (Global.getSector() == null) return;

        if (!Global.getSector().hasTransientScript(BetterPatrolsScript.class)) {
            Global.getSector().addTransientScript(new BetterPatrolsScript());
        }
    }
}

package data.scripts;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;

/**
 * Makes friendly patrols treat a much stronger, transponder-off player fleet the
 * same way a weaker hostile fleet would: by avoiding it instead of hailing it.
 *
 * The game's own tactical AI checks MEMORY_KEY_AVOID_PLAYER_SLOWLY for
 * non-hostile fleets, causing them to disengage and keep their distance.
 */
public class BetterPatrolsScript implements EveryFrameScript {

    /** How many times stronger the player's fleet must be before patrols avoid it. */
    public static final float STRENGTH_RATIO = 1.5f;

    /** Seconds between checks. */
    public static final float CHECK_INTERVAL = 0.5f;

    /**
     * Safety timeout, in campaign days. The flag is refreshed every check while
     * active and auto-clears if this script stops updating the fleet (for
     * example if the fleet leaves the player's location or the save is loaded
     * without the mod active).
     */
    public static final float MEMORY_EXPIRE_DAYS = 1f;

    /** Marks fleets this script has modified, so vanilla/other mod flags are not removed. */
    private static final String MARKER = "$betterPatrolsAvoidingPlayer";

    private float timer = 0f;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        timer += amount;
        if (timer < CHECK_INTERVAL) return;
        timer = 0f;

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player == null) return;

        LocationAPI location = player.getContainingLocation();
        if (location == null) return;

        boolean playerTransponderOff = !player.isTransponderOn();
        float playerStrength = getFleetStrength(player);

        List<CampaignFleetAPI> fleets = location.getFleets();
        for (CampaignFleetAPI fleet : fleets) {
            if (fleet == player) continue;

            MemoryAPI memory = fleet.getMemoryWithoutUpdate();
            boolean qualifies = playerTransponderOff && qualifies(fleet, player, playerStrength);

            if (qualifies) {
                memory.set(MARKER, true, MEMORY_EXPIRE_DAYS);
                memory.set(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY, true, MEMORY_EXPIRE_DAYS);
            } else if (memory.getBoolean(MARKER)) {
                memory.unset(MARKER);
                memory.unset(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY);
            }
        }
    }

    private boolean qualifies(CampaignFleetAPI fleet, CampaignFleetAPI player, float playerStrength) {
        if (!fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET)) return false;
        if (fleet.getFaction().isPlayerFaction()) return false;
        if (fleet.getFaction().isHostileTo(player.getFaction())) return false;
        if (fleet.isHostileTo(player)) return false;

        float fleetStrength = getFleetStrength(fleet);
        return fleetStrength > 0f && playerStrength > fleetStrength * STRENGTH_RATIO;
    }

    private float getFleetStrength(CampaignFleetAPI fleet) {
        float strength = 0f;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            strength += Misc.getMemberStrength(member, true, true, true);
        }
        return strength;
    }
}

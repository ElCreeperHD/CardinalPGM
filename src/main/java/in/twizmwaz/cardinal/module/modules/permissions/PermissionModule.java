package in.twizmwaz.cardinal.module.modules.permissions;

import com.google.common.base.Optional;
import in.twizmwaz.cardinal.Cardinal;
import in.twizmwaz.cardinal.GameHandler;
import in.twizmwaz.cardinal.event.MatchEndEvent;
import in.twizmwaz.cardinal.event.MatchStartEvent;
import in.twizmwaz.cardinal.event.PlayerChangeTeamEvent;
import in.twizmwaz.cardinal.module.Module;
import in.twizmwaz.cardinal.module.modules.team.TeamModule;
import in.twizmwaz.cardinal.util.Teams;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class PermissionModule implements Module {

    private static Plugin plugin;
    private static Map<UUID, PermissionAttachment> attachmentMap = new HashMap<>();

    private static List<UUID> developers = Arrays.asList(

    private static List<OfflinePlayer> muted = new ArrayList<>();

    public PermissionModule(Plugin plugin) {
        PermissionModule.plugin = plugin;
    }

    public static boolean isDeveloper(UUID player) {
        return GameHandler.getGameHandler().getMatch().getModules().getModule(PermissionModule.class).getDevelopers().contains(player);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    public List<UUID> getDevelopers() {
        return developers;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        attachmentMap.put(event.getPlayer().getUniqueId(), event.getPlayer().addAttachment(plugin));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (attachmentMap.containsKey(event.getPlayer().getUniqueId())) {
            event.getPlayer().removeAttachment(attachmentMap.get(event.getPlayer().getUniqueId()));
            attachmentMap.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerChangeTeam(PlayerChangeTeamEvent event) {
        if (Cardinal.getInstance().getConfig().getBoolean("worldEditPermissions")) {
            if ((event.getNewTeam().isPresent() && event.getNewTeam().get().isObserver()) || !GameHandler.getGameHandler().getMatch().isRunning()) {
                setWorldeditPermissions(event.getPlayer(), true);
            } else {
                setWorldeditPermissions(event.getPlayer(), false);
            }
        }
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        if (Cardinal.getInstance().getConfig().getBoolean("worldEditPermissions")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Optional<TeamModule> team = Teams.getTeamByPlayer(player);
                if ((team.isPresent() && team.get().isObserver())) {
                    setWorldeditPermissions(player, true);
                } else {
                    setWorldeditPermissions(player, false);
                }
            }
        }
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        if (Cardinal.getInstance().getConfig().getBoolean("worldEditPermissions")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                setWorldeditPermissions(player, true);
            }
        }
    }

    public PermissionAttachment getPlayerAttachment(Player player) {
        return attachmentMap.get(player.getUniqueId());
    }

    public void disablePermission(Player player, String permission) {
        attachmentMap.get(player.getUniqueId()).unsetPermission(permission);
    }

    public void enablePermission(Player player, String permission) {
        attachmentMap.get(player.getUniqueId()).setPermission(permission, true);
    }

    public void mute(Player player) {
        if (!muted.contains(player)) {
            muted.add(player);
            disablePermission(player, "cardinal.chat.team");
            disablePermission(player, "cardinal.chat.global");
        }
    }

    public void unmute(Player player) {
        if (muted.contains(player)) {
            muted.remove(player);
            enablePermission(player, "cardinal.chat.team");
            enablePermission(player, "cardinal.chat.global");
        }
    }

    public boolean isMuted(Player player) {
        return muted.contains(player);
    }

    public void setWorldeditPermissions(Player player, boolean state) {
        attachmentMap.get(player.getUniqueId()).setPermission("worldedit.navigation.jumpto.tool", state);
        attachmentMap.get(player.getUniqueId()).setPermission("worldedit.navigation.thru.tool", state);

        attachmentMap.get(player.getUniqueId()).setPermission("worldedit.navigation.jumpto.command", state);
        attachmentMap.get(player.getUniqueId()).setPermission("worldedit.navigation.thru.command", state);

        attachmentMap.get(player.getUniqueId()).setPermission("worldedit.navigation.unstuck", state);
    }

}

package dev.plex.listener.impl;

import dev.plex.cache.PlayerCache;
import dev.plex.listener.PlexListener;
import dev.plex.player.PlexPlayer;
import dev.plex.rank.enums.Rank;
import dev.plex.util.PlexUtils;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class ChatListener extends PlexListener
{
    private final PlexChatRenderer renderer = new PlexChatRenderer();

    @EventHandler
    public void onChat(AsyncChatEvent event)
    {
        PlexPlayer plexPlayer = PlayerCache.getPlexPlayerMap().get(event.getPlayer().getUniqueId());
        if (PlayerCache.getPunishedPlayer(event.getPlayer().getUniqueId()).isMuted())
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("muted"));
            event.setCancelled(true);
            return;
        }

        String prefix = plugin.getRankManager().getPrefix(plexPlayer);
        if (!prefix.isEmpty())
        {
            renderer.hasPrefix = true;
            renderer.prefix = prefix;
        }
        else
        {
            renderer.hasPrefix = false;
            renderer.prefix = null;
        }
        event.renderer(renderer);
    }

    public static void adminChat(CommandSender sender, String message)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (plugin.getSystem().equalsIgnoreCase("ranks"))
            {
                PlexPlayer plexPlayer = PlayerCache.getPlexPlayerMap().get(player.getUniqueId());
                if (plexPlayer.getRankFromString().isAtLeast(Rank.ADMIN))
                {
                    player.sendMessage(PlexUtils.messageComponent("adminChatFormat", sender.getName(), message));
                }
            }
            else if (plugin.getSystem().equalsIgnoreCase("permissions"))
            {
                if (player.hasPermission("plex.adminchat"))
                {
                    player.sendMessage(PlexUtils.messageComponent("adminChatFormat", sender.getName(), message));
                }
            }
        }
        plugin.getServer().getConsoleSender().sendMessage(PlexUtils.messageComponent("adminChatFormat", sender.getName(), message));
    }

    public static class PlexChatRenderer implements ChatRenderer
    {
        public boolean hasPrefix;
        public String prefix;

        @Override
        public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer)
        {
            if (hasPrefix)
            {
                return Component.empty().append(MiniMessage.miniMessage().deserialize(prefix))
                        .append(Component.space())
                        .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&" + plugin.config.getString("chat.name-color") + LegacyComponentSerializer.legacyAmpersand().serialize(sourceDisplayName)))
                        .append(Component.space())
                        .append(Component.text("»").color(NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(message);
            }
            return Component.empty()
                    .append(sourceDisplayName)
                    .append(Component.space())
                    .append(Component.text("»").color(NamedTextColor.GRAY))
                    .append(Component.space())
                    .append(message);
        }
    }
}

package net.simplyrin.statisticchecker;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.pikamug.localelib.LocaleLib;
import me.pikamug.localelib.LocaleManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.simplyrin.statisticchecker.commands.LogCommand;
import net.simplyrin.statisticchecker.commands.MergePlayerCommand;

/**
 * Created by SimplyRin on 2020/08/19.
 *
 * Copyright (c) 2020 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class Main extends JavaPlugin implements Listener {

	@Getter
	private LocaleManager localeManager;

	private LogCommand logCommand;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		this.logCommand = new LogCommand(this);
		this.getCommand("log").setExecutor(this.logCommand);
		this.getCommand("unti").setExecutor(this.logCommand);
		this.getCommand("unchi").setExecutor(this.logCommand);
		this.getCommand("mergeplayer").setExecutor(new MergePlayerCommand(this));

		LocaleLib localeLib = (LocaleLib) this.getServer().getPluginManager().getPlugin("LocaleLib");
		this.localeManager = localeLib.getLocaleManager();

		this.getServer().getPluginManager().registerEvents(this, this);

		/* this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
			while (true) {
				int players = this.getServer().getOnlinePlayers().size();
				if (players >= 1) {
					this.update();

					StatisticBlock rank = this.logCommand.getTopUser(1);
					this.dispatchCommand("hd setline logRank 3 &e&l1. &b" + rank.getBlock() + "&7: &e&l" + rank.getSize());

					rank = this.logCommand.getTopUser(2);
					this.dispatchCommand("hd setline logRank 5 &e&l2. &b" + rank.getBlock() + "&7: &e&l" + rank.getSize());

					rank = this.logCommand.getTopUser(3);
					this.dispatchCommand("hd setline logRank 7 &e&l3. &b" + rank.getBlock() + "&7: &e&l" + rank.getSize());

					rank = this.logCommand.getTopUser(4);
					this.dispatchCommand("hd setline logRank 9 &e4. &b" + rank.getBlock() + "&7: &e&l" + rank.getSize());

					rank = this.logCommand.getTopUser(5);
					this.dispatchCommand("hd setline logRank 11 &e5. &b" + rank.getBlock() + "&7: &e&l" + rank.getSize());

					this.dispatchCommand("hd setline logRank 13 &7最終更新: " + new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date()));
				}

				try {
					TimeUnit.SECONDS.sleep(60);
				} catch (Exception e) {
				}
			}
		}); */

		this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
			while (true) {
				for (Player player : this.getServer().getOnlinePlayers()) {
					if (player.hasPermission("statistichecker.showactionbar")) {
						this.update(player);
						int total = this.getConfig().getInt("Player." + player.getUniqueId().toString() + ".Statistic.MINE_BLOCK_TOTAL");
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§b破壊数: " + String.format("%,d", total)));
					}
				}

				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (Exception e) {
				}
			}
		});
	}

	@Override
	public void onDisable() {
		this.saveConfig();
		this.getServer().getScheduler().cancelTasks(this);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		this.update();
	}

	public void update() {
		for (Player player : this.getServer().getOnlinePlayers()) {
			this.update(player);
		}
		this.saveConfig();
	}

	public void update(Player player) {
		this.getConfig().set("Player." + player.getUniqueId() + ".Name", player.getName());
		this.getConfig().set("Player." + player.getUniqueId() + ".UniqueId", player.getUniqueId().toString());
		int total = 0;
		for (Material material : Material.values()) {
			int mineBlock = player.getStatistic(Statistic.MINE_BLOCK, material);
			if (mineBlock >= 1) {
				this.getConfig().set("Player." + player.getUniqueId() + ".Statistic.MINE_BLOCK." + material.name(), mineBlock);
			}
			total += mineBlock;
		}
		this.getConfig().set("Player." + player.getUniqueId() + ".Statistic.MINE_BLOCK_TOTAL", total);
	}

	public void dispatchCommand(String command) {
		this.getServer().getScheduler().runTask(this, () -> {
			this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command);
		});
	}

}

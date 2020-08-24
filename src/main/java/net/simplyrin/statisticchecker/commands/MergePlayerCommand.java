package net.simplyrin.statisticchecker.commands;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import net.simplyrin.statisticchecker.Main;

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
@AllArgsConstructor
public class MergePlayerCommand implements CommandExecutor {

	/*
	 * プレイヤーの統計移行コマンド
	 * 移行後は移行前のプレイヤーの統計はすべて消える
	 */

	private Main instance;

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("statisticchecker.admin.merge")) {
			sender.hasPermission("§cYou don't have permission to access this command!");
			return true;
		}

		if (args.length > 1) {
			Player oldPlayer = this.instance.getServer().getPlayer(args[0]);
			Player newPlayer = this.instance.getServer().getPlayer(args[1]);

			for (Statistic statistic : Statistic.values()) {
				sender.sendMessage("§7Migrating " + statistic.name() + "...");
				switch (statistic) {
				case MINE_BLOCK:
					for (Material material : Material.values()) {
						if (!material.isBlock()) {
							continue;
						}
						int oldValue = 0;
						try {
							oldValue = oldPlayer.getStatistic(statistic, material);
						} catch (Exception e) {
						}
						int newValue = 0;
						try {
							newValue = newPlayer.getStatistic(statistic, material);
						} catch (Exception e) {
						}

						if (oldValue >= 1 || newValue >= 1) {
							newPlayer.setStatistic(statistic, material, newValue + oldValue);
							oldPlayer.setStatistic(statistic, material, 0);
						}
					}
					break;
				case BREAK_ITEM:
				case CRAFT_ITEM:
				case USE_ITEM:
					for (Material material : Material.values()) {
						if (!material.isItem()) {
							continue;
						}
						int oldValue = 0;
						try {
							oldValue = oldPlayer.getStatistic(statistic, material);
						} catch (Exception e) {
						}
						int newValue = 0;
						try {
							newValue = newPlayer.getStatistic(statistic, material);
						} catch (Exception e) {
						}

						if (oldValue >= 1 || newValue >= 1) {
							newPlayer.setStatistic(statistic, material, newValue + oldValue);
							oldPlayer.setStatistic(statistic, material, 0);
						}
					}
					break;
				case PICKUP:
				case DROP:
					for (Material material : Material.values()) {
						int oldValue = 0;
						try {
							oldValue = oldPlayer.getStatistic(statistic, material);
						} catch (Exception e) {
						}
						int newValue = 0;
						try {
							newValue = newPlayer.getStatistic(statistic, material);
						} catch (Exception e) {
						}

						if (oldValue >= 1 || newValue >= 1) {
							newPlayer.setStatistic(statistic, material, newValue + oldValue);
							oldPlayer.setStatistic(statistic, material, 0);
						}
					}
					break;
				case ENTITY_KILLED_BY:
				case KILL_ENTITY:
					for (EntityType entityType : EntityType.values()) {
						int oldValue = 0;
						try {
							oldValue = oldPlayer.getStatistic(statistic, entityType);
						} catch (Exception e) {
						}
						int newValue = 0;
						try {
							newValue = newPlayer.getStatistic(statistic, entityType);
						} catch (Exception e) {
						}

						if (oldValue >= 1 || newValue >= 1) {
							newPlayer.setStatistic(statistic, entityType, newValue + oldValue);
							oldPlayer.setStatistic(statistic, entityType, 0);
						}
					}
					break;
				default:
					int oldValue = 0;
					try {
						oldValue = oldPlayer.getStatistic(statistic);
					} catch (Exception e) {
					}
					int newValue = 0;
					try {
						newValue = newPlayer.getStatistic(statistic);
					} catch (Exception e) {
					}

					if (oldValue >= 1 || newValue >= 1) {
						newPlayer.setStatistic(statistic, newValue + oldValue);
						oldPlayer.setStatistic(statistic, 0);
					}
					break;
				}
				sender.sendMessage("§aMigrated " + statistic.name() + ".");
			}
			return true;
		}

		sender.sendMessage("§cUsage: /mergeplayer <old> <new>");
		return true;
	}

}

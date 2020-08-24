package net.simplyrin.statisticchecker.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
public class LogCommand implements CommandExecutor {

	private Main instance;

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.instance.update();

		if (sender instanceof Player) {
			Player player = (Player) sender;
			int mineBlock = this.instance.getConfig().getInt("Player." + player.getUniqueId() + ".Statistic.MINE_BLOCK_TOTAL");
			sender.sendMessage("§aあなたが破壊した累計ブロック数は " + String.format("%,d", mineBlock) + " です！");

			sender.sendMessage("§9§m------------------");

			int page = 5;
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("rank")) {
					for (int i = 1; i <= 20; i++) {
						StatisticBlock user = this.getTopUser(i);
						if (user != null) {
							BanList banList = this.instance.getServer().getBanList(Type.NAME);
							String tag = "";
							if (banList.isBanned(user.block)) {
								tag = "§c§m";
							}
							sender.sendMessage("§b" + tag + i + ". " + user.block + ": " + String.format("%,d", user.size));
						}
					}
					sender.sendMessage("§9§m------------------");
					return true;
				}
				Player target = this.instance.getServer().getPlayer(args[0]);
				if (target != null) {
					mineBlock = this.instance.getConfig().getInt("Player." + target.getUniqueId() + ".Statistic.MINE_BLOCK_TOTAL");
					sender.sendMessage("§a" + target.getName() + "が破壊した累計ブロック数は " + String.format("%,d", mineBlock) + " です！");
					if (args.length > 1) {
						try {
							page = Integer.valueOf(args[1]);
						} catch (Exception e) {
						}
					}
					for (int i = 1; i <= page; i++) {
						StatisticBlock block = this.getBlock(target.getUniqueId(), i);
						if (block != null) {
							this.instance.getLocaleManager().sendMessage(player, "§b" + i + ". " + String.format("%,d", block.size) + ": "
									+ "<item>", Material.getMaterial(block.block), (short) 0, null);
						}
					}
					sender.sendMessage("§9§m------------------");
					return true;
				}
				try {
					page = Integer.valueOf(args[0]);
				} catch (Exception e) {
				}
			}

			for (int i = 1; i <= page; i++) {
				StatisticBlock block = this.getBlock(player.getUniqueId(), i);
				if (block != null) {
					this.instance.getLocaleManager().sendMessage(player, "§b" + i + ". " + String.format("%,d", block.size) + ": "
							+ "<item>", Material.getMaterial(block.block), (short) 0, null);
				}
			}
			sender.sendMessage("§9§m------------------");
			sender.sendMessage("§cブロック破壊数: /log <数>");
			sender.sendMessage("§cランキング: /log rank");
		}
		return true;
	}

	public String replaceName(String name) {
		name = name.toLowerCase();
		name = name.substring(0, 1).toUpperCase() + name.substring(1);

		if (name.contains("_")) {
			String value = "";
			for (String split : name.split("_")) {
				value += split.substring(0, 1).toUpperCase() + split.substring(1) + " ";
			}
			name = value.trim();
		}

		return name;
	}

	public StatisticBlock getBlock(UUID uniqueId, int level) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (String key : this.instance.getConfig().getConfigurationSection("Player." + uniqueId.toString() + ".Statistic.MINE_BLOCK").getKeys(false)) {
			int size = this.instance.getConfig().getInt("Player." + uniqueId.toString() + ".Statistic.MINE_BLOCK." + key);
			map.put(key, size);
		}

		List<Map.Entry<String,Integer>> entries = new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return ((Integer) o2.getValue()).compareTo((Integer) o1.getValue());
			}
		});

		level--;

		try {
			StatisticBlock block = new StatisticBlock();
			block.block = entries.get(level).getKey();
			block.size = entries.get(level).getValue();
			return block;
		} catch (Exception e) {
			return null;
		}
	}

	public StatisticBlock getTopUser(int level) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (String uniqueId : this.instance.getConfig().getConfigurationSection("Player").getKeys(false)) {
			String name = this.instance.getConfig().getString("Player." + uniqueId + ".Name");
			int size = this.instance.getConfig().getInt("Player." + uniqueId + ".Statistic.MINE_BLOCK_TOTAL");
			map.put(name, size);
		}

		List<Map.Entry<String,Integer>> entries = new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return ((Integer) o2.getValue()).compareTo((Integer) o1.getValue());
			}
		});

		level--;

		try {
			StatisticBlock block = new StatisticBlock();
			block.block = entries.get(level).getKey();
			block.size = entries.get(level).getValue();
			return block;
		} catch (Exception e) {
			return null;
		}
	}

	public class StatisticBlock {
		@Getter
		String block;
		int size;

		public String getSize() {
			return String.format("%,d", this.size);
		}
	}

}

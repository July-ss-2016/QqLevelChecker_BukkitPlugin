package vip.creeper.mcserverplugins.qqlevelchecker;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import vip.creeper.mcserverplugins.creeperqqbinder.CreeperQqBinder;
import vip.creeper.mcserverplugins.creeperqqbinder.QqInfo;
import vip.creeper.mcserverplugins.creeperqqbinder.managers.QqBinderManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by July on 2018/2/6.
 */
public class QqLevelChecker extends JavaPlugin {
    private static QqLevelChecker instance;
    private Settings settings;
    private QqBinderManager qqBinderManager;
    private File cacheFile;
    private YamlConfiguration cacheYml;
    private Logger logger = getLogger();
    private static final String COOKIE = "pgv_pvi=2351035392; pt2gguin=o0884633197; RK=BIPXcCvqOS; ptcz=e8479f886d219fb0b47b757ca1b629939b8980f311da74e13798e34e17f29c14; pgv_pvid=2395417474; o_cookie=884633197; pac_uid=1_884633197; eas_sid=C1T5v1U4F5G9W5B4C7k2T3O008; ts_uid=3293448356; pgv_flv=-; tvfe_boss_uuid=9c3ec1fa1a8c480d; ts_refer=ADTAGCLIENT.QQ.5551_sviplevel.0; p_o2_uin=884633197; uin=o0884633197; skey=@aFo0KkXGQ; ptisp=ctc; pgv_info=ssid=s8771015395; ts_last=vip.qq.com/";

    public void onEnable() {
        instance = this;
        this.settings = new Settings();
        this.qqBinderManager = CreeperQqBinder.getInstance().getQqBinderManager();

        loadConfig();

        this.cacheFile = new File(getDataFolder(), "caches.yml");

        try {
            if (!cacheFile.exists() && !cacheFile.createNewFile()) {
                logger.warning("创建 caches.yml 失败!");
                setEnabled(false);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.cacheYml = YamlConfiguration.loadConfiguration(cacheFile);

        getCommand("qlc").setExecutor(this);
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();

        settings.setCookie(getConfig().getString("cookie"));
    }

    public boolean onCommand(CommandSender cs, Command cmd, String lable, String[] args) {
        if (!cs.hasPermission("QqLevelChecker.admin")) {
            cs.sendMessage("无权限.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();

            cs.sendMessage("ok.");
        }

        if (!(cs instanceof ConsoleCommandSender)) {
            cs.sendMessage("命令执行者必须是控制台!");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("clean") && isNumberStr(args[1])) {
            int minLimitLevel = Integer.parseInt(args[1]);

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                for (QqInfo qqInfo : qqBinderManager.getQqInfos()) {
                    String qq = qqInfo.getQq();
                    int cacheQqLevel = getCacheQqLevel(qq);
                    int currentQqLevel;

                    if (cacheQqLevel > minLimitLevel) {
                        continue;
                    }

                    currentQqLevel = getQqLevel(qq, settings.getCookie());

                    setCacheQqLevel(qq, currentQqLevel);

                    if (currentQqLevel == -1) {
                        logger.info("qq = " + qq + " 获取QQ等级失败.");
                        continue;
                    }

                    if (currentQqLevel < minLimitLevel) {
                        qqBinderManager.unbindQq(qq);
                        logger.info("qq = " + qq + " 已被解绑.");
                    }
                }

                logger.info("检测完毕!");
            });
        }

        return false;
    }

    private boolean setCacheQqLevel(String qq, int level) {
        cacheYml.set("qqs." + qq, level);

        try {
            cacheYml.save(cacheFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private int getCacheQqLevel(String qq) {
        return cacheYml.getInt("qqs." + qq, -1);
    }

    private static int getQqLevel(String qq, String cookie) {
        String response = HttpUtil.sendGet("http://vip.qq.com/pk/index?param=" + qq, cookie);
        int startIndex = response.indexOf("var GUEST_LEVEL_INFO = {\"");

        if (startIndex == -1) {
            return -1;
        }

        String qqInfo = response.substring(startIndex, response.length());
        String level = qqInfo.substring(qqInfo.indexOf("iQQLevel\":\"") + 11, qqInfo.indexOf("\",\"iQQSportStep"));

        return Integer.parseInt(level);
    }



    private boolean isNumberStr(String str) {
        return str.matches("[0-9]+");
    }
}

package dev.Hilligans.ourcraft.ModHandler;

import dev.Hilligans.ourcraft.GameInstance;
import dev.Hilligans.ourcraft.ModHandler.Content.ModContent;
import dev.Hilligans.ourcraft.Data.Primitives.Triplet;
import dev.Hilligans.ourcraft.Util.Settings;
import dev.Hilligans.ourcraft.WorldSave.WorldLoader;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModLoader {

    public String mod = "ourcraft";
    public GameInstance gameInstance;

    public HashMap<String, Triplet<Class<?>,String,Boolean>> mainClasses = new HashMap<>();

    public ModLoader(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void loadDefaultMods() {
        loadAllMods(new File("mods/"));
        loadClasses(new File("target/classes/"), "");
        gameInstance.CONTENT_PACK.load();
        gameInstance.CONTENT_PACK.generateData();
    }

    public void loadAllMods(File folder) {
        File[] mods = folder.listFiles();
        if(mods != null) {
            for (File mod : mods) {
                loadMod(mod);
            }
        }
    }

    public void loadClasses(File folder, String topName) {
        File[] mods = folder.listFiles();
        if(mods != null) {
            for (File mod : mods) {
                if(mod.isDirectory()) {
                    loadClasses(mod, topName + mod.getName() + ".");
                } else if(mod.getName().endsWith(".class")) {
                    try {
                        URLClassLoader child = new URLClassLoader(new URL[]{mod.toURI().toURL()}, this.getClass().getClassLoader());
                        Class<?> testClass = Class.forName(topName + mod.getName().substring(0,mod.getName().length() - 6),false,child);
                        JSONObject jsonObject = getContent(child);
                        String modID = getModID(testClass);
                        if(modID != null) {
                            this.mod = modID;
                            ModContent modContent = new ModContent(modID,gameInstance);
                            modContent.isJar = false;
                            modContent.path = folder.getPath();
                            if(jsonObject != null) {
                                modContent.readData(jsonObject);
                            }
                            modContent.mainClass = testClass;
                            modContent.addClassLoader(child);
                            mainClasses.put(modID,new Triplet<>(testClass,mod.getAbsolutePath(),false));
                            gameInstance.CONTENT_PACK.registerModContent(modContent);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }


    public boolean loadMod(File file) {
        try {
            URLClassLoader child = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
            ArrayList<String> classNames = getClassNames(file);
            JSONObject jsonObject = getContent(child);
            for(String name : classNames) {
                Class<?> testClass = Class.forName(name,false,child);
                String modID = getModID(testClass);
                if(modID != null) {
                    ModContent modContent = new ModContent(modID,gameInstance);
                    modContent.path = name;
                    if(jsonObject != null) {
                        modContent.readData(jsonObject);
                    }
                    mod = modID;
                    testClass = Class.forName(name,true,child);
                    mainClasses.put(modID,new Triplet<>(testClass,file.getAbsolutePath(),true));
                    modContent.mainClass = testClass;
                    modContent.addClassLoader(child);
                    gameInstance.CONTENT_PACK.registerModContent(modContent);
                    return true;
                }
            }
        } catch (Exception e) {
            mod = "";
            return false;
        }
        mod = "";
        return true;
    }

    public JSONObject getContent(URLClassLoader classLoader) throws Exception {
        try {
            return new JSONObject(WorldLoader.readString(classLoader.getResource("mod.json").openStream()));
        } catch (Exception e) {
            if(!Settings.loadModsWithoutInfo) {
                mod = "";
                throw new Exception("Cannot load file");
            }
        }
        return null;
    }

    //https://stackoverflow.com/questions/15720822/how-to-get-names-of-classes-inside-a-jar-file
    public ArrayList<String> getClassNames(File file) {
        ArrayList<String> classNames = new ArrayList<String>();
        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(file.getPath()));
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.');
                    classNames.add(className.substring(0, className.length() - ".class".length()));
                }
            }
        } catch (Exception ignored) {}
        return classNames;
    }

    public String getModID(Class<?> val) {
        for(Annotation annotation : val.getAnnotations()) {
            if(annotation.annotationType().equals(Mod.class)) {
                if(((Mod)annotation).modID().equals("")) {
                    System.out.println("MainClass " + val.toString() + " has no modID specified, will not load");
                    return null;
                } else {
                    return ((Mod)annotation).modID();
                }
            }
        }
        return null;
    }




}
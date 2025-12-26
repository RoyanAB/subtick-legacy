package cn.royan.subtick.utils.deobfuscator.yarn;


import cn.royan.subtick.SubtickMod;
import cn.royan.subtick.utils.deobfuscator.StackTraceDeobfuscator;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Copy from Carpet-TIS-Addition
 */

public class OnlineMappingProvider {
	private static final Logger LOGGER = SubtickMod.LOGGER;
	public static final String MINECRAFT_VERSION = FabricLoader.getInstance().getRawGameVersion();
	public static final String YARN_META_URL = "https://meta.ornithemc.net/v3/versions/feather/" + MINECRAFT_VERSION;
	public static final String YARN_MAPPING_URL_BASE = "https://maven.ornithemc.net/releases/net/ornithemc/feather/";
	public static final String MAPPINGS_JAR_LOCATION = "mappings/mappings.tiny";
	public static final String STORAGE_DIRECTORY = String.format("./config/%s/mapping/", SubtickMod.MOD_ID);
	public static final String YARN_VERSION_CACHE_FILE = STORAGE_DIRECTORY + "yarn_version.json";

	private static String getMappingFileName(String yarnVersion) {
		return String.format("feather-%s-v2", yarnVersion);
	}

	private static String getMappingFileNameFull(String yarnVersion) {
		return getMappingFileName(yarnVersion) + ".tiny";
	}

	private static String getYarnVersionOnline() throws IOException {
		URL url = URI.create(YARN_META_URL).toURL();
		URLConnection request = url.openConnection();
		List<Pair<Integer, String>> list = Lists.newArrayList();
		JsonElement json = JsonParser.parseReader(new InputStreamReader(request.getInputStream()));
		json.getAsJsonArray().forEach(e -> {
			JsonObject object = e.getAsJsonObject();
			list.add(new Pair<>(object.get("build").getAsInt(), object.get("version").getAsString()));
		});
		return list.stream().max(Comparator.comparingInt(Pair::getLeft)).orElseThrow(() -> new IOException("Empty list")).getRight();
	}

	synchronized private static String getYarnVersion(boolean useCache) throws IOException {
		List<YarnVersionCache> cacheList = Lists.newArrayList();

		// read
		File file = new File(YARN_VERSION_CACHE_FILE);
		if (isFile(file)) {
			YarnVersionCache[] caches = null;
			try {
				caches = new Gson().fromJson(new InputStreamReader(Files.newInputStream(file.toPath())), YarnVersionCache[].class);
			} catch (Exception e) {
				LOGGER.warn("Failed to deserialize data from {}: {}", YARN_VERSION_CACHE_FILE, e);
			}
			if (caches != null) {
				cacheList.addAll(Arrays.asList(caches));
			}
		}

		// scan
		YarnVersionCache storedCache = null;
		for (YarnVersionCache cache : cacheList) {
			if (cache.minecraftVersion.equals(OnlineMappingProvider.MINECRAFT_VERSION)) {
				storedCache = cache;
				break;
			}
		}
		if (useCache && storedCache != null) {
			LOGGER.debug("Found feather version from file cache");
			return storedCache.yarnVersion;
		}

		// download
		String yarnVersion = getYarnVersionOnline();
		cacheList.remove(storedCache);
		cacheList.add(new YarnVersionCache(OnlineMappingProvider.MINECRAFT_VERSION, yarnVersion));

		// store
		touchFileDirectory(file);
		OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()));
		writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(cacheList));
		writer.flush();
		writer.close();

		return yarnVersion;
	}

	synchronized private static FileInputStream getYarnMappingStream(String yarnVersion) throws IOException {
		File mappingFile = new File(STORAGE_DIRECTORY + getMappingFileNameFull(yarnVersion));
		if (!isFile(mappingFile)) {
			String mappingJar = String.format("%s.jar", getMappingFileName(yarnVersion));
			String mappingJarUrl = String.format("%s%s/%s", YARN_MAPPING_URL_BASE, yarnVersion, mappingJar);
			String escapedUrl = UrlEscapers.urlFragmentEscaper().escape(mappingJarUrl);

			LOGGER.info("Downloading yarn mapping from {}", escapedUrl);
			File jarFile = new File(STORAGE_DIRECTORY + mappingJar);
			org.apache.commons.io.FileUtils.copyURLToFile(URI.create(escapedUrl).toURL(), jarFile);

			try (FileSystem jar = FileSystems.newFileSystem(jarFile.toPath(), (ClassLoader) null)) {
				Files.copy(jar.getPath(MAPPINGS_JAR_LOCATION), mappingFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			Files.delete(jarFile.toPath());
		}
		return new FileInputStream(mappingFile);
	}

	synchronized private static void loadMappings(InputStream mappingStream, String yarnVersion) {
		if (StackTraceDeobfuscator.loadMappings(mappingStream, "Feather " + yarnVersion)) {
			LOGGER.info("Feather mapping file {} loaded", getMappingFileNameFull(yarnVersion));
		}
	}

	private static void getMappingThreaded() {
		try {
			// 1. Get yarn version
			String yarnVersion = getYarnVersion(true);
			LOGGER.debug("Got Feather version for Minecraft {}: {}", MINECRAFT_VERSION, yarnVersion);

			// 2. Get yarn mapping
			FileInputStream mappingStream = getYarnMappingStream(yarnVersion);

			// 3. Load yarn mapping
			loadMappings(mappingStream, yarnVersion);

		} catch (IOException e) {
			LOGGER.error("Failed to get feather mapping, the stack trace deobfuscator will not work: {}", e.toString());
		}
	}

	/**
	 * Entry point
	 */
	public static void getMapping() {
		startThread("Mapping", OnlineMappingProvider::getMappingThreaded);
	}

	public static void startThread(String threadName, Runnable runnable) {
		Thread thread = new Thread(runnable);
		if (threadName != null) {
			thread.setName(threadName);
		}
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * FileUtil
	 */
	public static void touchFileDirectory(File file) throws IOException {
		touchDirectory(file.getParentFile());
	}

	public static void touchDirectory(File dir) throws IOException {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("Directory creation failed");
			}
		} else if (!dir.isDirectory()) {
			throw new IOException("Directory exists but it's not a directory");
		}
	}

	public static boolean isFile(File file) {
		return file.exists() && file.isFile();
	}
}

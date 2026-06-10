package com.velox.module.system.log.ip;

import com.velox.module.system.log.config.SystemLogProperties;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class Ip2RegionLocationResolver implements IpLocationResolver {

    private static final Logger log = LoggerFactory.getLogger(Ip2RegionLocationResolver.class);
    private static final String DEFAULT_IPV4_XDB_PATH = "classpath:ip2region/ip2region_v4.xdb";
    private static final String DEFAULT_IPV6_XDB_PATH = "classpath:ip2region/ip2region_v6.xdb";

    private final SystemLogProperties properties;
    private final Searcher ipv4Searcher;
    private final Searcher ipv6Searcher;
    private final Map<String, IpLocation> cache;

    public Ip2RegionLocationResolver(SystemLogProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.ipv4Searcher = createSearcher(Version.IPv4, resolveV4Path(properties), resourceLoader);
        this.ipv6Searcher = createSearcher(Version.IPv6, resolveV6Path(properties), resourceLoader);
        int cacheSize = Math.max(0, properties.getIpLocation().getCacheSize());
        this.cache = new LinkedHashMap<>(128, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, IpLocation> eldest) {
                return cacheSize > 0 && size() > cacheSize;
            }
        };
    }

    @Override
    public IpLocation resolve(String ip) {
        String normalized = normalize(ip);
        if (!StringUtils.hasText(normalized)) {
            return unknown(null);
        }
        synchronized (cache) {
            IpLocation cached = cache.get(normalized);
            if (cached != null) {
                return cached;
            }
        }
        IpLocation resolved = doResolve(normalized);
        synchronized (cache) {
            cache.put(normalized, resolved);
        }
        return resolved;
    }

    private IpLocation doResolve(String ip) {
        if (!properties.getIpLocation().isEnabled()) {
            return unknown(ip);
        }
        if (isIntranet(ip)) {
            IpLocation location = base(ip);
            location.setCountryName(properties.getIpLocation().getIntranetText());
            location.setLocationText(properties.getIpLocation().getIntranetText());
            location.setSource(properties.getIpLocation().getProvider());
            return location;
        }
        Searcher searcher = searcher(ip);
        if (searcher == null) {
            return unknown(ip);
        }
        try {
            String region = searcher.search(ip);
            return fromRegion(ip, region);
        } catch (Exception exception) {
            log.warn("Failed to resolve ip location: {}", ip, exception);
            return unknown(ip);
        }
    }

    private IpLocation fromRegion(String ip, String region) {
        IpLocation location = base(ip);
        location.setSource(properties.getIpLocation().getProvider());
        if (!StringUtils.hasText(region)) {
            location.setLocationText(properties.getIpLocation().getUnknownText());
            return location;
        }
        String[] parts = region.split("\\|");
        location.setCountryName(part(parts, 0));
        location.setCountryCode(part(parts, 4));
        location.setProvinceName(part(parts, 1));
        location.setCityName(part(parts, 2));
        location.setIsp(part(parts, 3));
        location.setLocationText(join(location.getCountryName(), location.getProvinceName(), location.getCityName()));
        if (!StringUtils.hasText(location.getLocationText())) {
            location.setLocationText(properties.getIpLocation().getUnknownText());
        }
        return location;
    }

    private IpLocation unknown(String ip) {
        IpLocation location = base(ip);
        location.setLocationText(properties.getIpLocation().getUnknownText());
        location.setSource(properties.getIpLocation().getProvider());
        return location;
    }

    private IpLocation base(String ip) {
        IpLocation location = new IpLocation();
        location.setIpVersion(resolveVersion(ip));
        location.setParsedAt(LocalDateTime.now(ZoneOffset.UTC));
        return location;
    }

    private boolean isIntranet(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress();
        } catch (Exception exception) {
            return false;
        }
    }

    private String resolveVersion(String ip) {
        if (!StringUtils.hasText(ip)) {
            return null;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address instanceof Inet4Address) {
                return "IPv4";
            }
            if (address instanceof Inet6Address) {
                return "IPv6";
            }
        } catch (Exception ignored) {
        }
        return ip.contains(":") ? "IPv6" : "IPv4";
    }

    private Searcher searcher(String ip) {
        return ip.contains(":") ? ipv6Searcher : ipv4Searcher;
    }

    private String resolveV4Path(SystemLogProperties properties) {
        if (StringUtils.hasText(properties.getIpLocation().getXdbPath())) {
            return properties.getIpLocation().getXdbPath();
        }
        return StringUtils.hasText(properties.getIpLocation().getXdbV4Path())
                ? properties.getIpLocation().getXdbV4Path()
                : DEFAULT_IPV4_XDB_PATH;
    }

    private String resolveV6Path(SystemLogProperties properties) {
        return StringUtils.hasText(properties.getIpLocation().getXdbV6Path())
                ? properties.getIpLocation().getXdbV6Path()
                : DEFAULT_IPV6_XDB_PATH;
    }

    private Searcher createSearcher(Version version, String xdbPath, ResourceLoader resourceLoader) {
        if (!properties.getIpLocation().isEnabled()) {
            return null;
        }
        if (!StringUtils.hasText(xdbPath)) {
            return null;
        }
        try {
            Resource resource = resourceLoader.getResource(xdbPath);
            if (!resource.exists()) {
                log.warn("{} xdb resource not found: {}", version.name, xdbPath);
                return null;
            }
            File file = materialize(resource);
            return Searcher.newWithFileOnly(version, file.getAbsolutePath());
        } catch (Exception exception) {
            log.warn("Failed to initialize {} ip2region searcher", version.name, exception);
            return null;
        }
    }

    private File materialize(Resource resource) throws Exception {
        if (resource.isFile()) {
            return resource.getFile();
        }
        File temp = File.createTempFile("velox-ip2region-", ".xdb");
        temp.deleteOnExit();
        try (InputStream input = resource.getInputStream(); FileOutputStream output = new FileOutputStream(temp)) {
            input.transferTo(output);
        }
        return temp;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String first = value.split(",")[0].trim();
        return first.isEmpty() ? null : first;
    }

    private String part(String[] parts, int index) {
        if (parts.length <= index) {
            return null;
        }
        String value = parts[index];
        if (!StringUtils.hasText(value) || "0".equals(value)) {
            return null;
        }
        return value.trim();
    }

    private String join(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(" / ");
            }
            builder.append(value.trim());
        }
        return builder.toString();
    }
}

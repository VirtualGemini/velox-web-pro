package com.velox.module.system.log.ip;

import com.velox.module.system.log.config.SystemLogProperties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class Ip2RegionLocationResolverTest {

    @Test
    void shouldReturnIntranetBeforeXdbLookup() {
        SystemLogProperties properties = new SystemLogProperties();
        properties.getIpLocation().setXdbV4Path("classpath:missing-v4.xdb");
        properties.getIpLocation().setXdbV6Path("classpath:missing-v6.xdb");
        Ip2RegionLocationResolver resolver = new Ip2RegionLocationResolver(properties, new DefaultResourceLoader());

        IpLocation location = resolver.resolve("127.0.0.1");

        assertThat(location.getLocationText()).isEqualTo("intranet");
        assertThat(location.getCountryName()).isEqualTo("intranet");
        assertThat(location.getIpVersion()).isEqualTo("IPv4");
    }

    @Test
    void shouldReturnUnknownWhenPublicIpXdbMissing() {
        SystemLogProperties properties = new SystemLogProperties();
        properties.getIpLocation().setXdbV4Path("classpath:missing-v4.xdb");
        properties.getIpLocation().setXdbV6Path("classpath:missing-v6.xdb");
        Ip2RegionLocationResolver resolver = new Ip2RegionLocationResolver(properties, new DefaultResourceLoader());

        IpLocation location = resolver.resolve("8.8.8.8");

        assertThat(location.getLocationText()).isEqualTo("unknown");
        assertThat(location.getIpVersion()).isEqualTo("IPv4");
    }

    @Test
    void shouldResolvePublicIpv4WithBundledXdb() {
        SystemLogProperties properties = new SystemLogProperties();
        Ip2RegionLocationResolver resolver = new Ip2RegionLocationResolver(properties, new DefaultResourceLoader());

        IpLocation location = resolver.resolve("8.8.8.8");

        assertThat(location.getLocationText()).isNotBlank();
        assertThat(location.getLocationText()).isNotEqualTo("unknown");
        assertThat(location.getCountryName()).isNotBlank();
        assertThat(location.getIpVersion()).isEqualTo("IPv4");
    }

    @Test
    void shouldResolvePublicIpv6WithBundledXdb() {
        SystemLogProperties properties = new SystemLogProperties();
        Ip2RegionLocationResolver resolver = new Ip2RegionLocationResolver(properties, new DefaultResourceLoader());

        IpLocation location = resolver.resolve("2001:4860:4860::8888");

        assertThat(location.getLocationText()).isNotBlank();
        assertThat(location.getLocationText()).isNotEqualTo("unknown");
        assertThat(location.getCountryName()).isNotBlank();
        assertThat(location.getIpVersion()).isEqualTo("IPv6");
    }
}

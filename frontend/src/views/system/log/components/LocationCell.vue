<template>
  <div class="location-cell">
    <span>{{ locationText }}</span>
    <span v-if="ispText" class="isp-text">{{ ispText }}</span>
  </div>
</template>

<script setup lang="ts">
  const UNKNOWN_TEXT = 'unknown'

  const props = defineProps<{
    ipLocation?: string
    countryName?: string
    provinceName?: string
    cityName?: string
    isp?: string
  }>()

  const locationText = computed(() => {
    const location = normalizeLocationPart(props.ipLocation)
    if (location) return location

    const parts = [props.countryName, props.provinceName, props.cityName]
      .map(normalizeLocationPart)
      .filter((part): part is string => !!part && part !== UNKNOWN_TEXT)

    return parts.length > 0 ? parts.join(' / ') : UNKNOWN_TEXT
  })

  const ispText = computed(() => normalizeLocationPart(props.isp))

  function normalizeLocationPart(value?: string) {
    const text = value?.trim()
    if (!text) return undefined
    return text.toLowerCase() === UNKNOWN_TEXT ? UNKNOWN_TEXT : text
  }
</script>

<style scoped>
  .location-cell {
    display: grid;
    gap: 2px;
    min-width: 0;
    line-height: 1.35;
  }

  .isp-text {
    font-size: 0.9em;
    opacity: 0.7;
  }
</style>

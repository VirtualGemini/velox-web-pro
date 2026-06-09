<template>
  <div class="location-cell">
    <span>{{ text }}</span>
  </div>
</template>

<script setup lang="ts">
  const UNKNOWN_TEXT = 'unknown'

  const props = defineProps<{
    ipLocation?: string
    countryName?: string
    provinceName?: string
    cityName?: string
  }>()

  const text = computed(() => {
    const location = normalizeLocationPart(props.ipLocation)
    if (location) return location

    const parts = [props.countryName, props.provinceName, props.cityName]
      .map(normalizeLocationPart)
      .filter((part): part is string => !!part && part !== UNKNOWN_TEXT)

    return parts.length > 0 ? parts.join(' / ') : UNKNOWN_TEXT
  })

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
</style>

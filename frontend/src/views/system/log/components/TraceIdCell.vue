<template>
  <ElText v-if="traceId" class="trace-id" truncated :title="traceId">{{ traceId }}</ElText>
  <span v-else>-</span>
</template>

<script setup lang="ts">
  defineProps<{ traceId?: string }>()
</script>

<style scoped>
  .trace-id {
    display: inline-block;
    max-width: 180px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  }
</style>

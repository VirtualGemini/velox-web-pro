<!-- 布局容器 -->
<template>
  <div class="app-layout">
    <aside id="app-sidebar">
      <VeloxSidebarMenu />
    </aside>

    <main id="app-main">
      <div id="app-header">
        <VeloxHeaderBar />
      </div>
      <div id="app-content">
        <VeloxPageContent />
      </div>
    </main>

    <div id="app-global">
      <VeloxGlobalComponent />
    </div>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'AppLayout' })
</script>

<style lang="scss" scoped>
  @use './style';
</style>

<template>
  <div class="app-root">
    <router-view />
  </div>
</template>

<style>
html,
body,
#app,
.app-root {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}

.app-root {
  isolation: isolate;
}
</style>

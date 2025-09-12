<template>
  <div :style="{zIndex:zIndex,height:height,width:width}" class="pan-item">
    <div class="pan-info">
      <div class="pan-info-roles-container">
        <slot />
      </div>
    </div>
    <div :style="{backgroundImage: `url(${image})`}" class="pan-thumb" />
  </div>
</template>

<script>
export default {
  name: 'PanThumb',
  props: {
    image: {
      type: String,
      required: true
    },
    zIndex: {
      type: Number,
      default: 1
    },
    width: {
      type: String,
      default: '150px'
    },
    height: {
      type: String,
      default: '150px'
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.pan-item {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  display: inline-block;
  position: relative;
  cursor: default;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.pan-info-roles-container {
  padding: 20px;
  text-align: center;
}

.pan-thumb {
  width: 100%;
  height: 100%;
  background-size: cover;
  border-radius: 50%;
  overflow: hidden;
  position: absolute;
  transform-origin: 95% 40%;
  transition: all 0.3s ease-in-out;
}

.pan-thumb:after {
  content: '';
  width: 8px;
  height: 8px;
  position: absolute;
  border-radius: 50%;
  top: 40%;
  left: 95%;
  margin: -4px 0 0 -4px;
  background: radial-gradient(circle at 50% 50%, rgba(76, 172, 254, 0.8), rgba(76, 172, 254, 0.1));
  box-shadow: 0 0 1px rgba(255, 255, 255, 0.9);
}

.pan-item:hover .pan-thumb {
  transform: rotate(-110deg);
}

.pan-item:hover .pan-info p a {
  opacity: 1;
}

.pan-info {
  position: absolute;
  width: inherit;
  height: inherit;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: inset 0 0 0 5px rgba(0, 0, 0, 0.05);
}

.pan-info h3 {
  color: #fff;
  text-transform: uppercase;
  position: relative;
  letter-spacing: 2px;
  font-size: 18px;
  margin: 0 60px;
  padding: 22px 0 0 0;
  height: 85px;
  font-family: 'Open Sans', Arial, sans-serif;
  text-shadow: 0 0 1px #fff, 0 1px 2px rgba(0, 0, 0, 0.3);
}

.pan-info p {
  color: #fff;
  padding: 10px 5px;
  font-style: italic;
  margin: 0 30px;
  font-size: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.5);
  opacity: 0;
  transition: opacity 1s ease-in-out 0.4s;
}

.pan-info p a {
  display: block;
  color: rgba(255, 255, 255, 0.7);
  font-style: normal;
  font-weight: 700;
  text-transform: uppercase;
  font-size: 9px;
  letter-spacing: 1px;
  padding-top: 4px;
  font-family: 'Open Sans', Arial, sans-serif;
}

.pan-info p a:hover {
  color: rgba(255, 242, 34, 0.8);
}
</style>
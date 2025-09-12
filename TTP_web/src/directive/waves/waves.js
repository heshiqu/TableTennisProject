import Vue from 'vue'

const waves = Vue.directive('waves', {
  bind(el, binding) {
    el.addEventListener('click', e => {
      const customOpts = Object.assign({}, binding.value)
      const opts = Object.assign({
        ele: el, // 波纹作用元素
        type: 'hit', // hit 点击位置扩散 center中心点扩展
        color: 'rgba(0, 0, 0, 0.15)' // 波纹颜色
      }, customOpts)

      const target = opts.ele
      if (target) {
        target.style.position = 'relative'
        target.style.overflow = 'hidden'
        const rect = target.getBoundingClientRect()
        let ripple = target.querySelector('.waves-ripple')
        if (!ripple) {
          ripple = document.createElement('span')
          ripple.className = 'waves-ripple'
          ripple.style.cssText = `
            position: absolute;
            border-radius: 100%;
            background-color: ${opts.color};
            transform: scale(0);
            animation: waves-animation 0.6s ease-out;
            pointer-events: none;
          `
          target.appendChild(ripple)
        }

        const size = Math.max(rect.width, rect.height)
        ripple.style.width = ripple.style.height = size + 'px'

        if (opts.type === 'hit') {
          const x = e.clientX - rect.left - size / 2
          const y = e.clientY - rect.top - size / 2
          ripple.style.left = x + 'px'
          ripple.style.top = y + 'px'
        } else if (opts.type === 'center') {
          ripple.style.left = (rect.width - size) / 2 + 'px'
          ripple.style.top = (rect.height - size) / 2 + 'px'
        }

        setTimeout(() => {
          if (ripple && ripple.parentNode) {
            ripple.parentNode.removeChild(ripple)
          }
        }, 600)
      }
    })
  }
})

// 添加CSS动画
const style = document.createElement('style')
style.type = 'text/css'
style.innerHTML = `
@keyframes waves-animation {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}
`
document.getElementsByTagName('head')[0].appendChild(style)

export default waves
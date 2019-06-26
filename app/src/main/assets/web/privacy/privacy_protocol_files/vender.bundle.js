;/*!src/static/lib/pageSwitch.js*/
/*
 * pageSwitch
 * @author qiqiboy
 * @github https://github.com/qiqiboy/pageSwitch
 */
;
(function (ROOT, struct, undefined) {
  "use strict";

  var VERSION = '2.3.2';
  var lastTime = 0,
    nextFrame = ROOT.requestAnimationFrame ||
      ROOT.webkitRequestAnimationFrame ||
      ROOT.mozRequestAnimationFrame ||
      ROOT.msRequestAnimationFrame ||
      function (callback) {
        var currTime = +new Date,
          delay = Math.max(1000 / 60, 1000 / 60 - (currTime - lastTime));
        lastTime = currTime + delay;
        return setTimeout(callback, delay);
      },
    cancelFrame = ROOT.cancelAnimationFrame ||
      ROOT.webkitCancelAnimationFrame ||
      ROOT.webkitCancelRequestAnimationFrame ||
      ROOT.mozCancelRequestAnimationFrame ||
      ROOT.msCancelRequestAnimationFrame ||
      clearTimeout,
    DOC = ROOT.document,
    divstyle = DOC.createElement('div').style,
    cssVendor = function () {
      var tests = "-webkit- -moz- -o- -ms-".split(" "),
        prop;
      while (prop = tests.shift()) {
        if (camelCase(prop + 'transform') in divstyle) {
          return prop;
        }
      }
      return '';
    }(),
    opacity = cssTest('opacity'),
    transform = cssTest('transform'),
    perspective = cssTest('perspective'),
    transformStyle = cssTest('transform-style'),
    transformOrigin = cssTest('transform-origin'),
    backfaceVisibility = cssTest('backface-visibility'),
    preserve3d = transformStyle && function () {
      divstyle[transformStyle] = 'preserve-3d';
      return divstyle[transformStyle] == 'preserve-3d';
    }(),
    toString = Object.prototype.toString,
    slice = [].slice,
    class2type = {},
    event2type = {},
    event2code = {
      click: 4,
      mousewheel: 5,
      dommousescroll: 5,
      keydown: 6
    },
    POINTERTYPES = {
      2: 'touch',
      3: 'pen',
      4: 'mouse',
      pen: 'pen'
    },
    STARTEVENT = [],
    MOVEEVENT = [],
    EVENT = function () {
      var ret = {},
        states = {
          start: 1,
          down: 1,
          move: 2,
          end: 3,
          up: 3,
          cancel: 3
        };
      each("mouse touch pointer MSPointer-".split(" "), function (prefix) {
        var _prefix = /pointer/i.test(prefix) ? 'pointer' : prefix;
        ret[_prefix] = ret[_prefix] || {};
        POINTERTYPES[_prefix] = _prefix;
        each(states, function (endfix, code) {
          var ev = camelCase(prefix + endfix);
          ret[_prefix][ev] = code;
          event2type[ev.toLowerCase()] = _prefix;
          event2code[ev.toLowerCase()] = code;
          if (code == 1) {
            STARTEVENT.push(ev);
          } else {
            MOVEEVENT.push(ev);
          }
        });
      });
      return ret;
    }(),
    POINTERS = {
      touch: {},
      pointer: {},
      mouse: {}
    },
    EASE = {
      linear: function (t, b, c, d) { return c * t / d + b; },
      ease: function (t, b, c, d) { return -c * ((t = t / d - 1) * t * t * t - 1) + b; },
      'ease-in': function (t, b, c, d) { return c * (t /= d) * t * t + b; },
      'ease-out': function (t, b, c, d) { return c * ((t = t / d - 1) * t * t + 1) + b; },
      'ease-in-out': function (t, b, c, d) { if ((t /= d / 2) < 1) return c / 2 * t * t * t + b; return c / 2 * ((t -= 2) * t * t + 2) + b; },
      bounce: function (t, b, c, d) { if ((t /= d) < (1 / 2.75)) { return c * (7.5625 * t * t) + b; } else if (t < (2 / 2.75)) { return c * (7.5625 * (t -= (1.5 / 2.75)) * t + .75) + b; } else if (t < (2.5 / 2.75)) { return c * (7.5625 * (t -= (2.25 / 2.75)) * t + .9375) + b; } else { return c * (7.5625 * (t -= (2.625 / 2.75)) * t + .984375) + b; } }
    },
    TRANSITION = {
      /* 更改切换效果
       * @param Element cpage 当前页面
       * @param Float cp      当前页面过度百分比
       * @param Element tpage 前序页面
       * @param Float tp      前序页面过度百分比
       */
      fade: function (cpage, cp, tpage, tp) {
        if (opacity) {
          cpage.style.opacity = 1 - Math.abs(cp);
          if (tpage) {
            tpage.style.opacity = Math.abs(cp);
          }
        } else {
          cpage.style.filter = 'alpha(opacity=' + (1 - Math.abs(cp)) * 100 + ')';
          if (tpage) {
            tpage.style.filter = 'alpha(opacity=' + Math.abs(cp) * 100 + ')';
          }
        }
      }
    };

  each("Boolean Number String Function Array Date RegExp Object Error".split(" "), function (name) {
    class2type["[object " + name + "]"] = name.toLowerCase();
  });

  each("X Y ".split(" "), function (name) {
    var XY = { X: 'left', Y: 'top' },
      fire3D = perspective ? ' translateZ(0)' : '';

    TRANSITION['scroll' + name] = function (cpage, cp, tpage, tp) {
      var prop = name || ['X', 'Y'][this.direction];
      transform ? cpage.style[transform] = 'translate' + prop + '(' + cp * 100 + '%)' + fire3D : cpage.style[XY[prop]] = cp * 100 + '%';
      if (tpage) {
        transform ? tpage.style[transform] = 'translate' + prop + '(' + tp * 100 + '%)' + fire3D : tpage.style[XY[prop]] = tp * 100 + '%';
      }
    }

    TRANSITION['scroll3d' + name] = function (cpage, cp, tpage, tp) {
      var prop = name || ['X', 'Y'][this.direction],
        fix = cp < 0 ? -1 : 1,
        abscp = Math.abs(cp),
        deg;
      if (perspective) {
        if (abscp < .05) {
          deg = abscp * 1200;
          cp = 0; tp = fix * -1;
        } else if (abscp < .95) {
          deg = 60;
          cp = (cp - .05 * fix) / .9;
          tp = (tp + .05 * fix) / .9;
        } else {
          deg = (1 - abscp) * 1200;
          cp = fix; tp = 0;
        }
        cpage.parentNode.style[transform] = 'perspective(1000px) rotateX(' + deg + 'deg)';
        cpage.style[transform] = 'translate' + prop + '(' + cp * 100 + '%)';
        if (tpage) {
          tpage.style[transform] = 'translate' + prop + '(' + tp * 100 + '%)';
        }
      } else TRANSITION['scroll' + name].apply(this, arguments);
    }

    TRANSITION['slide' + name] = function (cpage, cp, tpage, tp) {
      TRANSITION['slideCoverReverse' + name].apply(this, arguments);
    }

    TRANSITION['flow' + name] = function (cpage, cp, tpage, tp) {
      TRANSITION['flowCoverIn' + name].apply(this, arguments);
    }

    TRANSITION['slice' + name] = function () {
      var createWrap = function (node, container) {
        var wrap = DOC.createElement('div');
        wrap.style.cssText = 'position:absolute;top:0;left:0;height:100%;width:100%;overflow:hidden;';
        wrap.appendChild(node);
        container.appendChild(wrap);
      },
        fixBlock = function (cpage, tpage, pages, container) {
          each(pages, function (page) {
            if (page.parentNode == container) return;
            if (cpage != page && tpage != page) {
              page.parentNode.style.display = 'none';
            } else {
              page.parentNode.style.display = 'block';
            }
          });
        };

      return function (cpage, cp, tpage, tp) {
        var prop = name || ['X', 'Y'][this.direction],
          len = prop == 'X' ? 'width' : 'height',
          total = this.container[camelCase('client-' + len)],
          m = Math.abs(cp) * 100,
          n = Math.abs(tp) * 100,
          end = cp == 0 || tp == 0;

        cpage.style[len] = end ? '100%' : total + 'px';
        if (cpage.parentNode == this.container) {
          createWrap(cpage, this.container);
        }
        cpage.parentNode.style.zIndex = cp > 0 ? 0 : 1;
        cpage.parentNode.style[len] = (Math.min(cp, 0) + 1) * 100 + '%';

        if (tpage) {
          tpage.style[len] = end ? '100%' : total + 'px';
          if (tpage.parentNode == this.container) {
            createWrap(tpage, this.container);
          }
          tpage.parentNode.style.zIndex = cp > 0 ? 1 : 0;
          tpage.parentNode.style[len] = (Math.min(tp, 0) + 1) * 100 + '%';
        }

        fixBlock(cpage, tpage, this.pages, this.container);
      }
    }();

    TRANSITION['flip' + name] = function (cpage, cp, tpage, tp) {
      var prop = name || ['X', 'Y'][1 - this.direction],
        fix = prop == 'X' ? -1 : 1;
      if (perspective) {
        cpage.style[backfaceVisibility] = 'hidden';
        cpage.style[transform] = 'perspective(1000px) rotate' + prop + '(' + cp * 180 * fix + 'deg)' + fire3D;
        if (tpage) {
          tpage.style[backfaceVisibility] = 'hidden';
          tpage.style[transform] = 'perspective(1000px) rotate' + prop + '(' + tp * 180 * fix + 'deg)' + fire3D;
        }
      } else TRANSITION['scroll' + name].apply(this, arguments);
    }

    TRANSITION['flip3d' + name] = function () {
      var inited;
      return function (cpage, cp, tpage, tp) {
        var prop = name || ['X', 'Y'][1 - this.direction],
          fe = prop == 'X' ? -1 : 1,
          fix = fe * (cp < 0 ? 1 : -1),
          zh = cpage['offset' + (prop == 'X' ? 'Height' : 'Width')] / 2;
        if (preserve3d) {
          if (!inited) {
            inited = true;
            cpage.parentNode.parentNode.style[perspective] = '1000px';
            cpage.parentNode.style[transformStyle] = 'preserve-3d';
          }
          cpage.parentNode.style[transform] = 'translateZ(-' + zh + 'px) rotate' + prop + '(' + cp * 90 * fe + 'deg)';
          cpage.style[transform] = 'rotate' + prop + '(0) translateZ(' + zh + 'px)';
          if (tpage) {
            tpage.style[transform] = 'rotate' + prop + '(' + (fix * 90) + 'deg) translateZ(' + zh + 'px)';
          }
        } else TRANSITION['scroll' + name].apply(this, arguments);
      }
    }();

    TRANSITION['flipClock' + name] = function () {
      var createWrap = function (node, container, prop, off) {
        var wrap = node.parentNode,
          len = prop == 'X' ? 'height' : 'width',
          pos = prop == 'X' ? 'top' : 'left',
          origin = ['50%', (off ? 0 : 100) + '%'][prop == 'X' ? 'slice' : 'reverse']().join(' ');

        if (!wrap || wrap == container) {
          wrap = DOC.createElement('div');
          wrap.style.cssText = 'position:absolute;top:0;left:0;height:100%;width:100%;overflow:hidden;display:none;';
          wrap.style[transformOrigin] = origin;
          wrap.style[backfaceVisibility] = 'hidden';
          wrap.appendChild(node);
          container.appendChild(wrap);
        }

        wrap.style[len] = '50%';
        wrap.style[pos] = off * 100 + '%';
        node.style[len] = '200%';
        node.style[pos] = -off * 200 + '%';

        return wrap;
      },
        fixBlock = function (cpage, tpage, pages, container) {
          each(pages, function (page) {
            if (page.parentNode == container) return;
            if (cpage != page && tpage != page) {
              page.parentNode.style.display = page._clone.parentNode.style.display = 'none';
            } else {
              page.parentNode.style.display = page._clone.parentNode.style.display = 'block';
            }
          });
        };

      return function (cpage, cp, tpage, tp) {
        var prop = name || ['X', 'Y'][1 - this.direction],
          isSelf = this.pages[this.current] == cpage,
          zIndex = Number(Math.abs(cp) < .5),
          fix = prop == 'X' ? 1 : -1,
          m, n;
        if (perspective) {
          createWrap(cpage, this.container, prop, 0);
          createWrap(cpage._clone || (cpage._clone = cpage.cloneNode(true)), this.container, prop, .5);

          m = n = -cp * 180 * fix;
          cp > 0 ? n = 0 : m = 0;
          cpage.parentNode.style.zIndex = cpage._clone.parentNode.style.zIndex = zIndex;
          cpage.parentNode.style[transform] = 'perspective(1000px) rotate' + prop + '(' + m + 'deg)';
          cpage._clone.parentNode.style[transform] = 'perspective(1000px) rotate' + prop + '(' + n + 'deg)';

          if (tpage) {
            createWrap(tpage, this.container, prop, 0);
            createWrap(tpage._clone || (tpage._clone = tpage.cloneNode(true)), this.container, prop, .5);

            m = n = -tp * 180 * fix;
            cp > 0 ? m = 0 : n = 0;
            tpage.parentNode.style.zIndex = tpage._clone.parentNode.style.zIndex = 1 - zIndex;
            tpage.parentNode.style[transform] = 'perspective(1000px) rotate' + prop + '(' + m + 'deg)';
            tpage._clone.parentNode.style[transform] = 'perspective(1000px) rotate' + prop + '(' + n + 'deg)';
          }

          fixBlock(cpage, tpage, this.pages, this.container);

          if (0 == cp || tp == 0) {
            cpage = this.pages[this.current];
            cpage.style.height = cpage.style.width = cpage.parentNode.style.height = cpage.parentNode.style.width = '100%';
            cpage.style.top = cpage.style.left = cpage.parentNode.style.top = cpage.parentNode.style.left = 0;
            cpage.parentNode.style.zIndex = 2;
          }
        } else TRANSITION['scroll' + name].apply(this, arguments);
      }
    }();

    TRANSITION['flipPaper' + name] = function () {
      var backDiv;

      return function (cpage, cp, tpage, tp) {
        var prop = name || ['X', 'Y'][this.direction],
          len = prop == 'X' ? 'width' : 'height',
          m = Math.abs(cp) * 100;
        if (!backDiv) {
          backDiv = DOC.createElement('div');
          backDiv.style.cssText = 'position:absolute;z-index:2;top:0;left:0;height:0;width:0;background:no-repeat #fff;';
          try {
            backDiv.style.backgroundImage = cssVendor + 'linear-gradient(' + (prop == 'X' ? 'right' : 'bottom') + ', #aaa 0,#fff 20px)';
          } catch (e) { }
          this.container.appendChild(backDiv);
        }

        TRANSITION['slice' + name].apply(this, arguments);

        backDiv.style.display = cp == 0 || tp == 0 ? 'none' : 'block';
        backDiv.style.width = backDiv.style.height = '100%';
        backDiv.style[len] = (cp < 0 ? m : 100 - m) + '%';
        backDiv.style[XY[prop]] = (cp < 0 ? 100 - 2 * m : 2 * m - 100) + '%';
      }
    }();

    TRANSITION['zoom' + name] = function (cpage, cp, tpage, tp) {
      var zIndex = Number(Math.abs(cp) < .5);
      if (transform) {
        cpage.style[transform] = 'scale' + name + '(' + Math.abs(1 - Math.abs(cp) * 2) + ')' + fire3D;
        cpage.style.zIndex = zIndex;
        if (tpage) {
          tpage.style[transform] = 'scale' + name + '(' + Math.abs(1 - Math.abs(cp) * 2) + ')' + fire3D;
          tpage.style.zIndex = 1 - zIndex;
        }
      } else TRANSITION['scroll' + name].apply(this, arguments);
    }

    TRANSITION['bomb' + name] = function (cpage, cp, tpage, tp) {
      var zIndex = Number(Math.abs(cp) < .5),
        val = Math.abs(1 - Math.abs(cp) * 2);
      if (transform) {
        cpage.style[transform] = 'scale' + name + '(' + (2 - val) + ')' + fire3D;
        cpage.style.opacity = zIndex ? val : 0;
        cpage.style.zIndex = zIndex;
        if (tpage) {
          tpage.style[transform] = 'scale' + name + '(' + (2 - val) + ')' + fire3D;
          tpage.style.opacity = zIndex ? 0 : val;
          tpage.style.zIndex = 1 - zIndex;
        }
      } else TRANSITION['scroll' + name].apply(this, arguments);
    }

    TRANSITION['skew' + name] = function (cpage, cp, tpage, tp) {
      var zIndex = Number(Math.abs(cp) < .5);
      if (transform) {
        cpage.style[transform] = 'skew' + name + '(' + cp * 180 + 'deg)' + fire3D;
        cpage.style.zIndex = zIndex;
        if (tpage) {
          tpage.style[transform] = 'skew' + name + '(' + tp * 180 + 'deg)' + fire3D;
          tpage.style.zIndex = 1 - zIndex;
        }
      } else TRANSITION['scroll' + name].apply(this, arguments);
    }

    each(" Reverse In Out".split(" "), function (type) {
      TRANSITION['scrollCover' + type + name] = function (cpage, cp, tpage, tp) {
        var prop = name || ['X', 'Y'][this.direction],
          zIndex = Number(type == 'In' || !type && cp < 0 || type == 'Reverse' && cp > 0),
          cr = 100, tr = 100;
        zIndex ? cr = 20 : tr = 20;
        transform ? cpage.style[transform] = 'translate' + prop + '(' + cp * cr + '%)' + fire3D : cpage.style[XY[prop]] = cp * cr + '%';
        cpage.style.zIndex = 1 - zIndex;
        if (tpage) {
          transform ? tpage.style[transform] = 'translate' + prop + '(' + tp * tr + '%)' + fire3D : tpage.style[XY[prop]] = tp * tr + '%';
          tpage.style.zIndex = zIndex;
        }
      }

      TRANSITION['slideCover' + type + name] = function (cpage, cp, tpage, tp) {
        var prop = name || ['X', 'Y'][this.direction],
          zIndex = Number(type == 'In' || !type && cp < 0 || type == 'Reverse' && cp > 0);
        if (transform) {
          cpage.style[transform] = 'translate' + prop + '(' + cp * (100 - zIndex * 100) + '%) scale(' + ((1 - Math.abs(zIndex && cp)) * .2 + .8) + ')' + fire3D;
          cpage.style.zIndex = 1 - zIndex;
          if (tpage) {
            tpage.style[transform] = 'translate' + prop + '(' + tp * zIndex * 100 + '%) scale(' + ((1 - Math.abs(zIndex ? 0 : tp)) * .2 + .8) + ')' + fire3D;
            tpage.style.zIndex = zIndex;
          }
        } else TRANSITION['scrollCover' + type + name].apply(this, arguments);
      }

      TRANSITION['flowCover' + type + name] = function (cpage, cp, tpage, tp) {
        var prop = name || ['X', 'Y'][this.direction],
          zIndex = Number(type == 'In' || !type && cp < 0 || type == 'Reverse' && cp > 0);
        if (transform) {
          cpage.style[transform] = 'translate' + prop + '(' + cp * (100 - zIndex * 50) + '%) scale(' + ((1 - Math.abs(cp)) * .5 + .5) + ')' + fire3D;
          cpage.style.zIndex = 1 - zIndex;
          if (tpage) {
            tpage.style[transform] = 'translate' + prop + '(' + tp * (50 + zIndex * 50) + '%) scale(' + ((1 - Math.abs(tp)) * .5 + .5) + ')' + fire3D;
            tpage.style.zIndex = zIndex;
          }
        } else TRANSITION['scrollCover' + type + name].apply(this, arguments);
      }

      TRANSITION['flipCover' + type + name] = function (cpage, cp, tpage, tp) {
        var prop = name || ['X', 'Y'][1 - this.direction],
          zIndex = Number(type == 'In' || !type && cp < 0 || type == 'Reverse' && cp > 0);
        if (perspective) {
          zIndex ? cp = 0 : tp = 0;
          cpage.style[transform] = 'perspective(1000px) rotate' + prop + '(' + cp * -90 + 'deg)' + fire3D;
          cpage.style.zIndex = 1 - zIndex;
          if (tpage) {
            tpage.style[transform] = 'perspective(1000px) rotate' + prop + '(' + tp * -90 + 'deg)' + fire3D;
            tpage.style.zIndex = zIndex;
          }
        } else TRANSITION['scroll' + name].apply(this, arguments);
      }

      TRANSITION['skewCover' + type + name] = function (cpage, cp, tpage, tp) {
        var zIndex = Number(type == 'In' || !type && cp < 0 || type == 'Reverse' && cp > 0);
        if (transform) {
          zIndex ? cp = 0 : tp = 0;
          cpage.style[transform] = 'skew' + name + '(' + cp * 90 + 'deg)' + fire3D;
          cpage.style.zIndex = 1 - zIndex;
          if (tpage) {
            tpage.style[transform] = 'skew' + name + '(' + tp * 90 + 'deg)' + fire3D;
            tpage.style.zIndex = zIndex;
          }
        } else TRANSITION['scroll' + name].apply(this, arguments);
      }

      TRANSITION['zoomCover' + type + name] = function (cpage, cp, tpage, tp) {
        var zIndex = Number(type == 'In' || !type && cp < 0 || type == 'Reverse' && cp > 0);
        if (transform) {
          zIndex ? cp = 0 : tp = 0;
          cpage.style[transform] = 'scale' + name + '(' + (1 - Math.abs(cp)) + ')' + fire3D;
          cpage.style.zIndex = 1 - zIndex;
          if (tpage) {
            tpage.style[transform] = 'scale' + name + '(' + (1 - Math.abs(tp)) + ')' + fire3D;
            tpage.style.zIndex = zIndex;
          }
        } else TRANSITION['scroll' + name].apply(this, arguments);
      }

      TRANSITION['bombCover' + type + name] = function (cpage, cp, tpage, tp) {
        var zIndex = Number(type == 'In' || !type && cp < 0 || type == 'Reverse' && cp > 0);
        if (transform) {
          zIndex ? cp = 0 : tp = 0;
          cpage.style[transform] = 'scale' + name + '(' + (1 + Math.abs(cp)) + ')' + fire3D;
          cpage.style.zIndex = 1 - zIndex;
          if (tpage) {
            tpage.style[transform] = 'scale' + name + '(' + (1 + Math.abs(tp)) + ')' + fire3D;
            tpage.style.zIndex = zIndex;
          }
          TRANSITION.fade.apply(this, arguments);
        } else TRANSITION['scroll' + name].apply(this, arguments);
      }
    });
  });

  function type(obj) {
    if (obj == null) {
      return obj + "";
    }

    return typeof obj == 'object' || typeof obj == 'function' ? class2type[toString.call(obj)] || "object" :
      typeof obj;
  }

  function isArrayLike(elem) {
    var tp = type(elem);
    return !!elem && tp != 'function' && tp != 'string' && (elem.length === 0 || elem.length && (elem.nodeType == 1 || (elem.length - 1) in elem));
  }

  function camelCase(str) {
    return (str + '').replace(/^-ms-/, 'ms-').replace(/-([a-z]|[0-9])/ig, function (all, letter) {
      return (letter + '').toUpperCase();
    });
  }

  function cssTest(name) {
    var prop = camelCase(name),
      _prop = camelCase(cssVendor + prop);
    return (prop in divstyle) && prop || (_prop in divstyle) && _prop || '';
  }

  function isFunction(func) {
    return type(func) == 'function';
  }

  function pointerLength(obj) {
    var len = 0, key;
    if (type(obj.length) == 'number') {
      len = obj.length;
    } else if ('keys' in Object) {
      len = Object.keys(obj).length;
    } else {
      for (key in obj) {
        if (obj.hasOwnProperty(key)) {
          len++;
        }
      }
    }
    return len;
  }

  function pointerItem(obj, n) {
    return 'item' in obj ? obj.item(n) : function () {
      var i = 0, key;
      for (key in this) {
        if (i++ == n) {
          return this[key];
        }
      }
    }.call(obj, n);
  }

  function each(arr, iterate) {
    if (isArrayLike(arr)) {
      if (type(arr.forEach) == 'function') {
        return arr.forEach(iterate);
      }
      var i = 0, len = arr.length, item;
      for (; i < len; i++) {
        item = arr[i];
        if (type(item) != 'undefined') {
          iterate(item, i, arr);
        }
      }
    } else {
      var key;
      for (key in arr) {
        iterate(key, arr[key], arr);
      }
    }
  }

  function children(elem) {
    var ret = [];
    each(elem.children || elem.childNodes, function (elem) {
      if (elem.nodeType == 1) {
        ret.push(elem);
      }
    });
    return ret;
  }

  function getStyle(elem, prop) {
    var style = ROOT.getComputedStyle && ROOT.getComputedStyle(elem, null) || elem.currentStyle || elem.style;
    return style[prop];
  }

  function addListener(elem, evstr, handler) {
    if (type(evstr) == 'object') {
      return each(evstr, function (evstr, handler) {
        addListener(elem, evstr, handler);
      });
    }
    each(evstr.split(" "), function (ev) {
      if (elem.addEventListener) {
        elem.addEventListener(ev, handler, false);
      } else if (elem.attachEvent) {
        elem.attachEvent('on' + ev, handler);
      } else elem['on' + ev] = handler;
    });
  }

  function offListener(elem, evstr, handler) {
    if (type(evstr) == 'object') {
      return each(evstr, function (evstr, handler) {
        offListener(elem, evstr, handler);
      });
    }
    each(evstr.split(" "), function (ev) {
      if (elem.removeEventListener) {
        elem.removeEventListener(ev, handler, false);
      } else if (elem.detachEvent) {
        elem.detachEvent('on' + ev, handler);
      } else elem['on' + ev] = null;
    });
  }

  function removeRange() {
    var range;
    if (ROOT.getSelection) {
      range = getSelection();
      if ('empty' in range) range.empty();
      else if ('removeAllRanges' in range) range.removeAllRanges();
    } else {
      DOC.selection.empty();
    }
  }

  function filterEvent(oldEvent) {
    var ev = {},
      which = oldEvent.which,
      button = oldEvent.button,
      pointers, pointer;

    each("wheelDelta detail which keyCode".split(" "), function (prop) {
      ev[prop] = oldEvent[prop];
    });

    ev.oldEvent = oldEvent;

    ev.type = oldEvent.type.toLowerCase();
    ev.eventType = event2type[ev.type] || ev.type;
    ev.eventCode = event2code[ev.type] || 0;
    ev.pointerType = POINTERTYPES[oldEvent.pointerType] || oldEvent.pointerType || ev.eventType;

    ev.target = oldEvent.target || oldEvent.srcElement || DOC.documentElement;
    if (ev.target.nodeType === 3) {
      ev.target = ev.target.parentNode;
    }

    ev.preventDefault = function () {
      oldEvent.preventDefault && oldEvent.preventDefault();
      ev.returnValue = oldEvent.returnValue = false;
    }

    if (pointers = POINTERS[ev.eventType]) {
      switch (ev.eventType) {
        case 'mouse':
        case 'pointer':
          var id = oldEvent.pointerId || 0;
          ev.eventCode == 3 ? delete pointers[id] : pointers[id] = oldEvent;
          break;
        case 'touch':
          POINTERS[ev.eventType] = pointers = oldEvent.touches;
          break;
      }

      if (pointer = pointerItem(pointers, 0)) {
        ev.clientX = pointer.clientX;
        ev.clientY = pointer.clientY;
      }

      ev.button = which < 4 ? Math.max(0, which - 1) : button & 4 && 1 || button & 2; // left:0 middle:1 right:2
      ev.length = pointerLength(pointers);
    }

    return ev;
  }

  struct.prototype = {
    version: VERSION,
    constructor: struct,
    latestTime: 0,
    init: function (config) {
      var self = this,
        handler = this.handler = function (ev) {
          !self.frozen && self.handleEvent(ev);
        }

      this.events = {};
      this.duration = isNaN(parseInt(config.duration)) ? 600 : parseInt(config.duration);
      this.direction = parseInt(config.direction) == 0 ? 0 : 1;
      this.current = parseInt(config.start) || 0;
      this.loop = !!config.loop;
      this.mouse = config.mouse == null ? true : !!config.mouse;
      this.mousewheel = !!config.mousewheel;
      this.interval = parseInt(config.interval) || 5000;
      this.playing = !!config.autoplay;
      this.arrowkey = !!config.arrowkey;
      this.frozen = !!config.freeze;
      this.pages = children(this.container);
      this.length = this.pages.length;

      this.pageData = [];

      addListener(this.container, STARTEVENT.join(" ") + " click" + (this.mousewheel ? " mousewheel DOMMouseScroll" : ""), handler);
      addListener(DOC, MOVEEVENT.join(" ") + (this.arrowkey ? " keydown" : ""), handler);

      each(this.pages, function (page) {
        self.pageData.push({
          percent: 0,
          cssText: page.style.cssText || ''
        });
        self.initStyle(page);
      });
      this.pages[this.current].style.display = 'block';

      this.on({
        before: function () { clearTimeout(this.playTimer); },
        dragStart: function () { clearTimeout(this.playTimer); removeRange(); },
        after: this.firePlay,
        update: null
      }).firePlay();

      // this.comment = document.createComment(' Powered by pageSwitch v' + this.version + '  https://github.com/qiqiboy/pageSwitch ');
      // this.container.appendChild(this.comment);

      this.setEase(config.ease);
      this.setTransition(config.transition);
    },
    initStyle: function (elem) {
      var style = elem.style,
        ret;
      each("position:absolute;top:0;left:0;width:100%;height:100%;display:none".split(";"), function (css) {
        ret = css.split(":");
        style[ret[0]] = ret[1];
      });
      return elem;
    },
    setEase: function (ease) {
      this.ease = isFunction(ease) ? ease : EASE[ease] || EASE.ease;
      return this;
    },
    addEase: function (name, func) {
      isFunction(func) && (EASE[name] = func);
      return this;
    },
    setTransition: function (transition) {
      this.events.update.splice(0, 1, isFunction(transition) ? transition : TRANSITION[transition] || TRANSITION.slide);
      return this;
    },
    addTransition: function (name, func) {
      isFunction(func) && (TRANSITION[name] = func);
      return this;
    },
    isStatic: function () {
      return !this.timer && !this.drag;
    },
    on: function (ev, callback) {
      var self = this;
      if (type(ev) == 'object') {
        each(ev, function (ev, callback) {
          self.on(ev, callback);
        });
      } else {
        if (!this.events[ev]) {
          this.events[ev] = [];
        }
        this.events[ev].push(callback);
      }
      return this;
    },
    fire: function (ev) {
      var self = this,
        args = slice.call(arguments, 1);
      each(this.events[ev] || [], function (func) {
        if (isFunction(func)) {
          func.apply(self, args);
        }
      });
      return this;
    },
    freeze: function (able) {
      this.frozen = able == null ? true : !!able;
      return this;
    },
    slide: function (index) {
      var self = this,
        dir = this.direction,
        duration = this.duration,
        stime = +new Date,
        ease = this.ease,
        current = this.current,
        fixIndex = Math.min(this.length - 1, Math.max(0, this.fixIndex(index))),
        cpage = this.pages[current],
        percent = this.getPercent(),
        tIndex = this.fixIndex(fixIndex == current ? current + (percent > 0 ? -1 : 1) : fixIndex),
        tpage = this.pages[tIndex],
        target = index > current ? -1 : 1,
        _tpage = cpage;

      cancelFrame(this.timer);

      if (fixIndex == current) {
        target = 0;
        _tpage = tpage;
      } else if (tpage.style.display == 'none') {
        percent = 0;
      }

      this.fixBlock(current, tIndex);
      this.fire('before', current, fixIndex);
      this.current = fixIndex;

      duration *= Math.abs(target - percent);

      this.latestTime = stime + duration;

      ani();

      function ani() {
        var offset = Math.min(duration, +new Date - stime),
          s = duration ? ease(offset, 0, 1, duration) : 1,
          cp = (target - percent) * s + percent;
        self.fixUpdate(cp, current, tIndex);
        if (offset == duration) {
          if (_tpage) {
            _tpage.style.display = 'none';
          }
          delete self.timer;
          self.fire('after', fixIndex, current);
        } else {
          self.timer = nextFrame(ani);
        }
      }

      return this;
    },
    prev: function () {
      return this.slide(this.current - 1);
    },
    next: function () {
      return this.slide(this.current + 1);
    },
    play: function () {
      this.playing = true;
      return this.firePlay();
    },
    firePlay: function () {
      var self = this;
      if (this.playing) {
        this.playTimer = setTimeout(function () {
          self.slide((self.current + 1) % (self.loop ? Infinity : self.length));
        }, this.interval);
      }
      return this;
    },
    pause: function () {
      this.playing = false;
      clearTimeout(this.playTimer);
      return this;
    },
    fixIndex: function (index) {
      return this.length > 1 && this.loop ? (this.length + index) % this.length : index;
    },
    fixBlock: function (cIndex, tIndex) {
      each(this.pages, function (page, index) {
        if (cIndex != index && tIndex != index) {
          page.style.display = 'none';
        } else {
          page.style.display = 'block';
        }
      });
      return this;
    },
    fixUpdate: function (cPer, cIndex, tIndex) {
      var pageData = this.pageData,
        cpage = this.pages[cIndex],
        tpage = this.pages[tIndex],
        tPer;
      pageData[cIndex].percent = cPer;
      if (tpage) {
        tPer = pageData[tIndex].percent = cPer > 0 ? cPer - 1 : 1 + cPer;
      }
      return this.fire('update', cpage, cPer, tpage, tPer);
    },
    getPercent: function (index) {
      var pdata = this.pageData[index == null ? this.current : index];
      return pdata && (pdata.percent || 0);
    },
    getOffsetParent: function () {
      var position = getStyle(this.container, 'position');
      if (position && position != 'static') {
        return this.container;
      }
      return this.container.offsetParent || DOC.body;
    },
    handleEvent: function (oldEvent) {
      var ev = filterEvent(oldEvent),
        canDrag = ev.button < 1 && ev.length < 2 && (!this.pointerType || this.pointerType == ev.eventType) && (this.mouse || ev.pointerType != 'mouse');
      switch (ev.eventCode) {
        case 2:
          if (canDrag && this.rect) {
            var cIndex = this.current,
              dir = this.direction,
              rect = [ev.clientX, ev.clientY],
              _rect = this.rect,
              offset = rect[dir] - _rect[dir],
              cpage = this.pages[cIndex],
              total = this.offsetParent[dir ? 'clientHeight' : 'clientWidth'],
              tIndex, percent;
            if (this.drag == null && _rect.toString() != rect.toString()) {
              this.drag = Math.abs(offset) >= Math.abs(rect[1 - dir] - _rect[1 - dir]);
              this.drag && this.fire('dragStart', ev);
            }
            if (this.drag) {
              percent = this.percent + (total && offset / total);
              if (!this.pages[tIndex = this.fixIndex(cIndex + (percent > 0 ? -1 : 1))]) {
                percent /= Math.abs(offset) / total + 2;
              }
              this.fixBlock(cIndex, tIndex);
              this.fire('dragMove', ev);
              this.fixUpdate(percent, cIndex, tIndex);
              this._offset = offset;
              ev.preventDefault();
            }
          }
          break;

        case 1:
        case 3:
          if (canDrag) {
            var self = this,
              index = this.current,
              percent = this.getPercent(),
              isDrag, offset, tm, nn;
            if (ev.length && (ev.eventCode == 1 || this.drag)) {
              nn = ev.target.nodeName.toLowerCase();
              clearTimeout(this.eventTimer);
              if (!this.pointerType) {
                this.pointerType = ev.eventType;
              }
              if (this.timer) {
                cancelFrame(this.timer);
                delete this.timer;
              }
              this.rect = [ev.clientX, ev.clientY];
              this.percent = percent;
              this.time = +new Date;
              this.offsetParent = this.getOffsetParent();
              if (ev.eventType != 'touch' && (nn == 'a' || nn == 'img')) {
                ev.preventDefault();
              }
            } else if (tm = this.time) {
              offset = this._offset;
              isDrag = this.drag;

              each("rect drag time percent _offset offsetParent".split(" "), function (prop) {
                delete self[prop];
              });

              if (isDrag) {
                if (+new Date - tm < 500 && Math.abs(offset) > 20 || Math.abs(percent) > .5) {
                  index += offset > 0 ? -1 : 1;
                }
                this.fire('dragEnd', ev);
                ev.preventDefault();
              }

              if (percent) {
                this.slide(index);
              } else if (isDrag) {
                this.firePlay();
              }

              this.eventTimer = setTimeout(function () {
                delete self.pointerType;
              }, 400);
            }
          }
          break;

        case 4:
          if (this.timer) {
            ev.preventDefault();
          }
          break;

        case 5:
          ev.preventDefault();
          if (this.isStatic() && +new Date - this.latestTime > Math.max(1000 - this.duration, 0)) {
            var wd = ev.wheelDelta || -ev.detail;
            Math.abs(wd) >= 3 && this[wd > 0 ? 'prev' : 'next']();
          }
          break;

        case 6:
          var nn = ev.target.nodeName.toLowerCase();
          if (this.isStatic() && nn != 'input' && nn != 'textarea' && nn != 'select') {
            switch (ev.keyCode || ev.which) {
              case 33:
              case 37:
              case 38:
                this.prev();
                break;
              case 32:
              case 34:
              case 39:
              case 40:
                this.next();
                break;
              case 35:
                this.slide(this.length - 1);
                break;
              case 36:
                this.slide(0);
                break;
            }
          }
          break;
      }
    },
    destroy: function () {
      var pageData = this.pageData;

      offListener(this.container, STARTEVENT.join(" ") + " click" + (this.mousewheel ? " mousewheel DOMMouseScroll" : ""), this.handler);
      offListener(DOC, MOVEEVENT.join(" ") + (this.arrowkey ? " keydown" : ""), this.handler);

      each(this.pages, function (page, index) {
        page.style.cssText = pageData[index].cssText;
      });

      this.container.removeChild(this.comment);

      this.length = 0;

      return this.pause();
    },
    append: function (elem, index) {
      if (null == index) {
        index = this.pages.length;
      }
      this.pageData.splice(index, 0, {
        percent: 0,
        cssText: elem.style.cssText
      });
      this.pages.splice(index, 0, elem);
      this.container.appendChild(this.initStyle(elem));

      this.length = this.pages.length;

      if (index <= this.current) {
        this.current++;
      }

      return this;
    },
    prepend: function (elem) {
      return this.append(elem, 0);
    },
    insertBefore: function (elem, index) {
      return this.append(elem, index - 1);
    },
    insertAfter: function (elem, index) {
      return this.append(elem, index + 1);
    },
    remove: function (index) {
      this.container.removeChild(this.pages[index]);
      this.pages.splice(index, 1);
      this.pageData.splice(index, 1);

      this.length = this.pages.length;

      if (index <= this.current) {
        this.slide(this.current = Math.max(0, this.current - 1));
      }

      return this;
    }
  }

  each("Ease Transition".split(" "), function (name) {
    struct['add' + name] = struct.prototype['add' + name];
  });

  if (typeof define == 'function' && define.amd) {
    define('pageSwitch', function () {
      return struct;
    });
  } else ROOT.pageSwitch = struct;

})(window, function (wrap, config) {
  if (!(this instanceof arguments.callee)) {
    return new arguments.callee(wrap, config);
  }

  this.container = typeof wrap == 'string' ? document.getElementById(wrap) : wrap;
  this.init(config || {});
});

;/*!src/static/lib/jquery.validate.min.js*/
/*! jQuery Validation Plugin - v1.17.0 - 7/29/2017 */
!function(a){"function"==typeof define&&define.amd?define(["jquery"],a):"object"==typeof module&&module.exports?module.exports=a(require("jquery")):a(jQuery)}(function(a){a.extend(a.fn,{validate:function(b){if(!this.length)return void(b&&b.debug&&window.console&&console.warn("Nothing selected, can't validate, returning nothing."));var c=a.data(this[0],"validator");return c?c:(this.attr("novalidate","novalidate"),c=new a.validator(b,this[0]),a.data(this[0],"validator",c),c.settings.onsubmit&&(this.on("click.validate",":submit",function(b){c.submitButton=b.currentTarget,a(this).hasClass("cancel")&&(c.cancelSubmit=!0),void 0!==a(this).attr("formnovalidate")&&(c.cancelSubmit=!0)}),this.on("submit.validate",function(b){function d(){var d,e;return c.submitButton&&(c.settings.submitHandler||c.formSubmitted)&&(d=a("<input type='hidden'/>").attr("name",c.submitButton.name).val(a(c.submitButton).val()).appendTo(c.currentForm)),!c.settings.submitHandler||(e=c.settings.submitHandler.call(c,c.currentForm,b),d&&d.remove(),void 0!==e&&e)}return c.settings.debug&&b.preventDefault(),c.cancelSubmit?(c.cancelSubmit=!1,d()):c.form()?c.pendingRequest?(c.formSubmitted=!0,!1):d():(c.focusInvalid(),!1)})),c)},valid:function(){var b,c,d;return a(this[0]).is("form")?b=this.validate().form():(d=[],b=!0,c=a(this[0].form).validate(),this.each(function(){b=c.element(this)&&b,b||(d=d.concat(c.errorList))}),c.errorList=d),b},rules:function(b,c){var d,e,f,g,h,i,j=this[0];if(null!=j&&(!j.form&&j.hasAttribute("contenteditable")&&(j.form=this.closest("form")[0],j.name=this.attr("name")),null!=j.form)){if(b)switch(d=a.data(j.form,"validator").settings,e=d.rules,f=a.validator.staticRules(j),b){case"add":a.extend(f,a.validator.normalizeRule(c)),delete f.messages,e[j.name]=f,c.messages&&(d.messages[j.name]=a.extend(d.messages[j.name],c.messages));break;case"remove":return c?(i={},a.each(c.split(/\s/),function(a,b){i[b]=f[b],delete f[b]}),i):(delete e[j.name],f)}return g=a.validator.normalizeRules(a.extend({},a.validator.classRules(j),a.validator.attributeRules(j),a.validator.dataRules(j),a.validator.staticRules(j)),j),g.required&&(h=g.required,delete g.required,g=a.extend({required:h},g)),g.remote&&(h=g.remote,delete g.remote,g=a.extend(g,{remote:h})),g}}}),a.extend(a.expr.pseudos||a.expr[":"],{blank:function(b){return!a.trim(""+a(b).val())},filled:function(b){var c=a(b).val();return null!==c&&!!a.trim(""+c)},unchecked:function(b){return!a(b).prop("checked")}}),a.validator=function(b,c){this.settings=a.extend(!0,{},a.validator.defaults,b),this.currentForm=c,this.init()},a.validator.format=function(b,c){return 1===arguments.length?function(){var c=a.makeArray(arguments);return c.unshift(b),a.validator.format.apply(this,c)}:void 0===c?b:(arguments.length>2&&c.constructor!==Array&&(c=a.makeArray(arguments).slice(1)),c.constructor!==Array&&(c=[c]),a.each(c,function(a,c){b=b.replace(new RegExp("\\{"+a+"\\}","g"),function(){return c})}),b)},a.extend(a.validator,{defaults:{messages:{},groups:{},rules:{},errorClass:"error",pendingClass:"pending",validClass:"valid",errorElement:"label",focusCleanup:!1,focusInvalid:!0,errorContainer:a([]),errorLabelContainer:a([]),onsubmit:!0,ignore:":hidden",ignoreTitle:!1,onfocusin:function(a){this.lastActive=a,this.settings.focusCleanup&&(this.settings.unhighlight&&this.settings.unhighlight.call(this,a,this.settings.errorClass,this.settings.validClass),this.hideThese(this.errorsFor(a)))},onfocusout:function(a){this.checkable(a)||!(a.name in this.submitted)&&this.optional(a)||this.element(a)},onkeyup:function(b,c){var d=[16,17,18,20,35,36,37,38,39,40,45,144,225];9===c.which&&""===this.elementValue(b)||a.inArray(c.keyCode,d)!==-1||(b.name in this.submitted||b.name in this.invalid)&&this.element(b)},onclick:function(a){a.name in this.submitted?this.element(a):a.parentNode.name in this.submitted&&this.element(a.parentNode)},highlight:function(b,c,d){"radio"===b.type?this.findByName(b.name).addClass(c).removeClass(d):a(b).addClass(c).removeClass(d)},unhighlight:function(b,c,d){"radio"===b.type?this.findByName(b.name).removeClass(c).addClass(d):a(b).removeClass(c).addClass(d)}},setDefaults:function(b){a.extend(a.validator.defaults,b)},messages:{required: "这是必填字段",remote: "请修正此字段",email: "请输入有效的电子邮件地址",url: "请输入有效的网址",date: "请输入有效的日期",dateISO: "请输入有效的日期 (YYYY-MM-DD)",number: "请输入有效的数字",digits: "只能输入数字",creditcard: "请输入有效的信用卡号码",equalTo: "你的输入不相同",extension: "请输入有效的后缀",maxlength: $.validator.format("最多可以输入{0}个字符"),minlength: $.validator.format("最少要输入{0}个字符"),rangelength: $.validator.format("请输入长度在{0}到{1}之间的字符串"),range: $.validator.format("请输入范围在{0}到{1}之间的数值"),max: $.validator.format("请输入不大于{0}的数值"),min: $.validator.format("请输入不小于{0}的数值"),step: $.validator.format("请输入一个{0}的倍数")},autoCreateRanges:!1,prototype:{init:function(){function b(b){!this.form&&this.hasAttribute("contenteditable")&&(this.form=a(this).closest("form")[0],this.name=a(this).attr("name"));var c=a.data(this.form,"validator"),d="on"+b.type.replace(/^validate/,""),e=c.settings;e[d]&&!a(this).is(e.ignore)&&e[d].call(c,this,b)}this.labelContainer=a(this.settings.errorLabelContainer),this.errorContext=this.labelContainer.length&&this.labelContainer||a(this.currentForm),this.containers=a(this.settings.errorContainer).add(this.settings.errorLabelContainer),this.submitted={},this.valueCache={},this.pendingRequest=0,this.pending={},this.invalid={},this.reset();var c,d=this.groups={};a.each(this.settings.groups,function(b,c){"string"==typeof c&&(c=c.split(/\s/)),a.each(c,function(a,c){d[c]=b})}),c=this.settings.rules,a.each(c,function(b,d){c[b]=a.validator.normalizeRule(d)}),a(this.currentForm).on("focusin.validate focusout.validate keyup.validate",":text, [type='password'], [type='file'], select, textarea, [type='number'], [type='search'], [type='tel'], [type='url'], [type='email'], [type='datetime'], [type='date'], [type='month'], [type='week'], [type='time'], [type='datetime-local'], [type='range'], [type='color'], [type='radio'], [type='checkbox'], [contenteditable], [type='button']",b).on("click.validate","select, option, [type='radio'], [type='checkbox']",b),this.settings.invalidHandler&&a(this.currentForm).on("invalid-form.validate",this.settings.invalidHandler)},form:function(){return this.checkForm(),a.extend(this.submitted,this.errorMap),this.invalid=a.extend({},this.errorMap),this.valid()||a(this.currentForm).triggerHandler("invalid-form",[this]),this.showErrors(),this.valid()},checkForm:function(){this.prepareForm();for(var a=0,b=this.currentElements=this.elements();b[a];a++)this.check(b[a]);return this.valid()},element:function(b){var c,d,e=this.clean(b),f=this.validationTargetFor(e),g=this,h=!0;return void 0===f?delete this.invalid[e.name]:(this.prepareElement(f),this.currentElements=a(f),d=this.groups[f.name],d&&a.each(this.groups,function(a,b){b===d&&a!==f.name&&(e=g.validationTargetFor(g.clean(g.findByName(a))),e&&e.name in g.invalid&&(g.currentElements.push(e),h=g.check(e)&&h))}),c=this.check(f)!==!1,h=h&&c,c?this.invalid[f.name]=!1:this.invalid[f.name]=!0,this.numberOfInvalids()||(this.toHide=this.toHide.add(this.containers)),this.showErrors(),a(b).attr("aria-invalid",!c)),h},showErrors:function(b){if(b){var c=this;a.extend(this.errorMap,b),this.errorList=a.map(this.errorMap,function(a,b){return{message:a,element:c.findByName(b)[0]}}),this.successList=a.grep(this.successList,function(a){return!(a.name in b)})}this.settings.showErrors?this.settings.showErrors.call(this,this.errorMap,this.errorList):this.defaultShowErrors()},resetForm:function(){a.fn.resetForm&&a(this.currentForm).resetForm(),this.invalid={},this.submitted={},this.prepareForm(),this.hideErrors();var b=this.elements().removeData("previousValue").removeAttr("aria-invalid");this.resetElements(b)},resetElements:function(a){var b;if(this.settings.unhighlight)for(b=0;a[b];b++)this.settings.unhighlight.call(this,a[b],this.settings.errorClass,""),this.findByName(a[b].name).removeClass(this.settings.validClass);else a.removeClass(this.settings.errorClass).removeClass(this.settings.validClass)},numberOfInvalids:function(){return this.objectLength(this.invalid)},objectLength:function(a){var b,c=0;for(b in a)void 0!==a[b]&&null!==a[b]&&a[b]!==!1&&c++;return c},hideErrors:function(){this.hideThese(this.toHide)},hideThese:function(a){a.not(this.containers).text(""),this.addWrapper(a).hide()},valid:function(){return 0===this.size()},size:function(){return this.errorList.length},focusInvalid:function(){if(this.settings.focusInvalid)try{a(this.findLastActive()||this.errorList.length&&this.errorList[0].element||[]).filter(":visible").focus().trigger("focusin")}catch(b){}},findLastActive:function(){var b=this.lastActive;return b&&1===a.grep(this.errorList,function(a){return a.element.name===b.name}).length&&b},elements:function(){var b=this,c={};return a(this.currentForm).find("input, select, textarea, [contenteditable]").not(":submit, :reset, :image, :disabled").not(this.settings.ignore).filter(function(){var d=this.name||a(this).attr("name");return!d&&b.settings.debug&&window.console&&console.error("%o has no name assigned",this),this.hasAttribute("contenteditable")&&(this.form=a(this).closest("form")[0],this.name=d),!(d in c||!b.objectLength(a(this).rules()))&&(c[d]=!0,!0)})},clean:function(b){return a(b)[0]},errors:function(){var b=this.settings.errorClass.split(" ").join(".");return a(this.settings.errorElement+"."+b,this.errorContext)},resetInternals:function(){this.successList=[],this.errorList=[],this.errorMap={},this.toShow=a([]),this.toHide=a([])},reset:function(){this.resetInternals(),this.currentElements=a([])},prepareForm:function(){this.reset(),this.toHide=this.errors().add(this.containers)},prepareElement:function(a){this.reset(),this.toHide=this.errorsFor(a)},elementValue:function(b){var c,d,e=a(b),f=b.type;return"radio"===f||"checkbox"===f?this.findByName(b.name).filter(":checked").val():"number"===f&&"undefined"!=typeof b.validity?b.validity.badInput?"NaN":e.val():(c=b.hasAttribute("contenteditable")?e.text():e.val(),"file"===f?"C:\\fakepath\\"===c.substr(0,12)?c.substr(12):(d=c.lastIndexOf("/"),d>=0?c.substr(d+1):(d=c.lastIndexOf("\\"),d>=0?c.substr(d+1):c)):"string"==typeof c?c.replace(/\r/g,""):c)},check:function(b){b=this.validationTargetFor(this.clean(b));var c,d,e,f,g=a(b).rules(),h=a.map(g,function(a,b){return b}).length,i=!1,j=this.elementValue(b);if("function"==typeof g.normalizer?f=g.normalizer:"function"==typeof this.settings.normalizer&&(f=this.settings.normalizer),f){if(j=f.call(b,j),"string"!=typeof j)throw new TypeError("The normalizer should return a string value.");delete g.normalizer}for(d in g){e={method:d,parameters:g[d]};try{if(c=a.validator.methods[d].call(this,j,b,e.parameters),"dependency-mismatch"===c&&1===h){i=!0;continue}if(i=!1,"pending"===c)return void(this.toHide=this.toHide.not(this.errorsFor(b)));if(!c)return this.formatAndAdd(b,e),!1}catch(k){throw this.settings.debug&&window.console&&console.log("Exception occurred when checking element "+b.id+", check the '"+e.method+"' method.",k),k instanceof TypeError&&(k.message+=".  Exception occurred when checking element "+b.id+", check the '"+e.method+"' method."),k}}if(!i)return this.objectLength(g)&&this.successList.push(b),!0},customDataMessage:function(b,c){return a(b).data("msg"+c.charAt(0).toUpperCase()+c.substring(1).toLowerCase())||a(b).data("msg")},customMessage:function(a,b){var c=this.settings.messages[a];return c&&(c.constructor===String?c:c[b])},findDefined:function(){for(var a=0;a<arguments.length;a++)if(void 0!==arguments[a])return arguments[a]},defaultMessage:function(b,c){"string"==typeof c&&(c={method:c});var d=this.findDefined(this.customMessage(b.name,c.method),this.customDataMessage(b,c.method),!this.settings.ignoreTitle&&b.title||void 0,a.validator.messages[c.method],"<strong>Warning: No message defined for "+b.name+"</strong>"),e=/\$?\{(\d+)\}/g;return"function"==typeof d?d=d.call(this,c.parameters,b):e.test(d)&&(d=a.validator.format(d.replace(e,"{$1}"),c.parameters)),d},formatAndAdd:function(a,b){var c=this.defaultMessage(a,b);this.errorList.push({message:c,element:a,method:b.method}),this.errorMap[a.name]=c,this.submitted[a.name]=c},addWrapper:function(a){return this.settings.wrapper&&(a=a.add(a.parent(this.settings.wrapper))),a},defaultShowErrors:function(){var a,b,c;for(a=0;this.errorList[a];a++)c=this.errorList[a],this.settings.highlight&&this.settings.highlight.call(this,c.element,this.settings.errorClass,this.settings.validClass),this.showLabel(c.element,c.message);if(this.errorList.length&&(this.toShow=this.toShow.add(this.containers)),this.settings.success)for(a=0;this.successList[a];a++)this.showLabel(this.successList[a]);if(this.settings.unhighlight)for(a=0,b=this.validElements();b[a];a++)this.settings.unhighlight.call(this,b[a],this.settings.errorClass,this.settings.validClass);this.toHide=this.toHide.not(this.toShow),this.hideErrors(),this.addWrapper(this.toShow).show()},validElements:function(){return this.currentElements.not(this.invalidElements())},invalidElements:function(){return a(this.errorList).map(function(){return this.element})},showLabel:function(b,c){var d,e,f,g,h=this.errorsFor(b),i=this.idOrName(b),j=a(b).attr("aria-describedby");h.length?(h.removeClass(this.settings.validClass).addClass(this.settings.errorClass),h.html(c)):(h=a("<"+this.settings.errorElement+">").attr("id",i+"-error").addClass(this.settings.errorClass).html(c||""),d=h,this.settings.wrapper&&(d=h.hide().show().wrap("<"+this.settings.wrapper+"/>").parent()),this.labelContainer.length?this.labelContainer.append(d):this.settings.errorPlacement?this.settings.errorPlacement.call(this,d,a(b)):d.insertAfter(b),h.is("label")?h.attr("for",i):0===h.parents("label[for='"+this.escapeCssMeta(i)+"']").length&&(f=h.attr("id"),j?j.match(new RegExp("\\b"+this.escapeCssMeta(f)+"\\b"))||(j+=" "+f):j=f,a(b).attr("aria-describedby",j),e=this.groups[b.name],e&&(g=this,a.each(g.groups,function(b,c){c===e&&a("[name='"+g.escapeCssMeta(b)+"']",g.currentForm).attr("aria-describedby",h.attr("id"))})))),!c&&this.settings.success&&(h.text(""),"string"==typeof this.settings.success?h.addClass(this.settings.success):this.settings.success(h,b)),this.toShow=this.toShow.add(h)},errorsFor:function(b){var c=this.escapeCssMeta(this.idOrName(b)),d=a(b).attr("aria-describedby"),e="label[for='"+c+"'], label[for='"+c+"'] *";return d&&(e=e+", #"+this.escapeCssMeta(d).replace(/\s+/g,", #")),this.errors().filter(e)},escapeCssMeta:function(a){if(a){return a.replace(/([\\!"#$%&'()*+,.\/:;<=>?@\[\]^`{|}~])/g,"\\$1")}},idOrName:function(a){return this.groups[a.name]||(this.checkable(a)?a.name:a.id||a.name)},validationTargetFor:function(b){return this.checkable(b)&&(b=this.findByName(b.name)),a(b).not(this.settings.ignore)[0]},checkable:function(a){return/radio|checkbox/i.test(a.type)},findByName:function(b){return a(this.currentForm).find("[name='"+this.escapeCssMeta(b)+"']")},getLength:function(b,c){switch(c.nodeName.toLowerCase()){case"select":return a("option:selected",c).length;case"input":if(this.checkable(c))return this.findByName(c.name).filter(":checked").length}return b.length},depend:function(a,b){return!this.dependTypes[typeof a]||this.dependTypes[typeof a](a,b)},dependTypes:{"boolean":function(a){return a},string:function(b,c){return!!a(b,c.form).length},"function":function(a,b){return a(b)}},optional:function(b){var c=this.elementValue(b);return!a.validator.methods.required.call(this,c,b)&&"dependency-mismatch"},startRequest:function(b){this.pending[b.name]||(this.pendingRequest++,a(b).addClass(this.settings.pendingClass),this.pending[b.name]=!0)},stopRequest:function(b,c){this.pendingRequest--,this.pendingRequest<0&&(this.pendingRequest=0),delete this.pending[b.name],a(b).removeClass(this.settings.pendingClass),c&&0===this.pendingRequest&&this.formSubmitted&&this.form()?(a(this.currentForm).submit(),this.submitButton&&a("input:hidden[name='"+this.submitButton.name+"']",this.currentForm).remove(),this.formSubmitted=!1):!c&&0===this.pendingRequest&&this.formSubmitted&&(a(this.currentForm).triggerHandler("invalid-form",[this]),this.formSubmitted=!1)},previousValue:function(b,c){return c="string"==typeof c&&c||"remote",a.data(b,"previousValue")||a.data(b,"previousValue",{old:null,valid:!0,message:this.defaultMessage(b,{method:c})})},destroy:function(){this.resetForm(),a(this.currentForm).off(".validate").removeData("validator").find(".validate-equalTo-blur").off(".validate-equalTo").removeClass("validate-equalTo-blur")}},classRuleSettings:{required:{required:!0},email:{email:!0},url:{url:!0},date:{date:!0},dateISO:{dateISO:!0},number:{number:!0},digits:{digits:!0},creditcard:{creditcard:!0}},addClassRules:function(b,c){b.constructor===String?this.classRuleSettings[b]=c:a.extend(this.classRuleSettings,b)},classRules:function(b){var c={},d=a(b).attr("class");return d&&a.each(d.split(" "),function(){this in a.validator.classRuleSettings&&a.extend(c,a.validator.classRuleSettings[this])}),c},normalizeAttributeRule:function(a,b,c,d){/min|max|step/.test(c)&&(null===b||/number|range|text/.test(b))&&(d=Number(d),isNaN(d)&&(d=void 0)),d||0===d?a[c]=d:b===c&&"range"!==b&&(a[c]=!0)},attributeRules:function(b){var c,d,e={},f=a(b),g=b.getAttribute("type");for(c in a.validator.methods)"required"===c?(d=b.getAttribute(c),""===d&&(d=!0),d=!!d):d=f.attr(c),this.normalizeAttributeRule(e,g,c,d);return e.maxlength&&/-1|2147483647|524288/.test(e.maxlength)&&delete e.maxlength,e},dataRules:function(b){var c,d,e={},f=a(b),g=b.getAttribute("type");for(c in a.validator.methods)d=f.data("rule"+c.charAt(0).toUpperCase()+c.substring(1).toLowerCase()),this.normalizeAttributeRule(e,g,c,d);return e},staticRules:function(b){var c={},d=a.data(b.form,"validator");return d.settings.rules&&(c=a.validator.normalizeRule(d.settings.rules[b.name])||{}),c},normalizeRules:function(b,c){return a.each(b,function(d,e){if(e===!1)return void delete b[d];if(e.param||e.depends){var f=!0;switch(typeof e.depends){case"string":f=!!a(e.depends,c.form).length;break;case"function":f=e.depends.call(c,c)}f?b[d]=void 0===e.param||e.param:(a.data(c.form,"validator").resetElements(a(c)),delete b[d])}}),a.each(b,function(d,e){b[d]=a.isFunction(e)&&"normalizer"!==d?e(c):e}),a.each(["minlength","maxlength"],function(){b[this]&&(b[this]=Number(b[this]))}),a.each(["rangelength","range"],function(){var c;b[this]&&(a.isArray(b[this])?b[this]=[Number(b[this][0]),Number(b[this][1])]:"string"==typeof b[this]&&(c=b[this].replace(/[\[\]]/g,"").split(/[\s,]+/),b[this]=[Number(c[0]),Number(c[1])]))}),a.validator.autoCreateRanges&&(null!=b.min&&null!=b.max&&(b.range=[b.min,b.max],delete b.min,delete b.max),null!=b.minlength&&null!=b.maxlength&&(b.rangelength=[b.minlength,b.maxlength],delete b.minlength,delete b.maxlength)),b},normalizeRule:function(b){if("string"==typeof b){var c={};a.each(b.split(/\s/),function(){c[this]=!0}),b=c}return b},addMethod:function(b,c,d){a.validator.methods[b]=c,a.validator.messages[b]=void 0!==d?d:a.validator.messages[b],c.length<3&&a.validator.addClassRules(b,a.validator.normalizeRule(b))},methods:{required:function(b,c,d){if(!this.depend(d,c))return"dependency-mismatch";if("select"===c.nodeName.toLowerCase()){var e=a(c).val();return e&&e.length>0}return this.checkable(c)?this.getLength(b,c)>0:b.length>0},email:function(a,b){return this.optional(b)||/^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/.test(a)},url:function(a,b){return this.optional(b)||/^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})).?)(?::\d{2,5})?(?:[\/?#]\S*)?$/i.test(a)},date:function(a,b){return this.optional(b)||!/Invalid|NaN/.test(new Date(a).toString())},dateISO:function(a,b){return this.optional(b)||/^\d{4}[\/\-](0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])$/.test(a)},number:function(a,b){return this.optional(b)||/^(?:-?\d+|-?\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/.test(a)},digits:function(a,b){return this.optional(b)||/^\d+$/.test(a)},minlength:function(b,c,d){var e=a.isArray(b)?b.length:this.getLength(b,c);return this.optional(c)||e>=d},maxlength:function(b,c,d){var e=a.isArray(b)?b.length:this.getLength(b,c);return this.optional(c)||e<=d},rangelength:function(b,c,d){var e=a.isArray(b)?b.length:this.getLength(b,c);return this.optional(c)||e>=d[0]&&e<=d[1]},min:function(a,b,c){return this.optional(b)||a>=c},max:function(a,b,c){return this.optional(b)||a<=c},range:function(a,b,c){return this.optional(b)||a>=c[0]&&a<=c[1]},step:function(b,c,d){var e,f=a(c).attr("type"),g="Step attribute on input type "+f+" is not supported.",h=["text","number","range"],i=new RegExp("\\b"+f+"\\b"),j=f&&!i.test(h.join()),k=function(a){var b=(""+a).match(/(?:\.(\d+))?$/);return b&&b[1]?b[1].length:0},l=function(a){return Math.round(a*Math.pow(10,e))},m=!0;if(j)throw new Error(g);return e=k(d),(k(b)>e||l(b)%l(d)!==0)&&(m=!1),this.optional(c)||m},equalTo:function(b,c,d){var e=a(d);return this.settings.onfocusout&&e.not(".validate-equalTo-blur").length&&e.addClass("validate-equalTo-blur").on("blur.validate-equalTo",function(){a(c).valid()}),b===e.val()},remote:function(b,c,d,e){if(this.optional(c))return"dependency-mismatch";e="string"==typeof e&&e||"remote";var f,g,h,i=this.previousValue(c,e);return this.settings.messages[c.name]||(this.settings.messages[c.name]={}),i.originalMessage=i.originalMessage||this.settings.messages[c.name][e],this.settings.messages[c.name][e]=i.message,d="string"==typeof d&&{url:d}||d,h=a.param(a.extend({data:b},d.data)),i.old===h?i.valid:(i.old=h,f=this,this.startRequest(c),g={},g[c.name]=b,a.ajax(a.extend(!0,{mode:"abort",port:"validate"+c.name,dataType:"json",data:g,context:f.currentForm,success:function(a){var d,g,h,j=a===!0||"true"===a;f.settings.messages[c.name][e]=i.originalMessage,j?(h=f.formSubmitted,f.resetInternals(),f.toHide=f.errorsFor(c),f.formSubmitted=h,f.successList.push(c),f.invalid[c.name]=!1,f.showErrors()):(d={},g=a||f.defaultMessage(c,{method:e,parameters:b}),d[c.name]=i.message=g,f.invalid[c.name]=!0,f.showErrors(d)),i.valid=j,f.stopRequest(c,j)}},d)),"pending")}}});var b,c={};return a.ajaxPrefilter?a.ajaxPrefilter(function(a,b,d){var e=a.port;"abort"===a.mode&&(c[e]&&c[e].abort(),c[e]=d)}):(b=a.ajax,a.ajax=function(d){var e=("mode"in d?d:a.ajaxSettings).mode,f=("port"in d?d:a.ajaxSettings).port;return"abort"===e?(c[f]&&c[f].abort(),c[f]=b.apply(this,arguments),c[f]):b.apply(this,arguments)}),a});

;/*!src/static/lib/cropper/cropper.js*/
/*!
 * Cropper v4.0.0
 * https://github.com/fengyuanchen/cropper
 *
 * Copyright (c) 2014-2018 Chen Fengyuan
 * Released under the MIT license
 *
 * Date: 2018-04-01T06:27:27.267Z
 * 
 * $.fn.cropper.setDefaults({
  checkImageOrigin: false
});
 */

(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? factory(require('jquery')) :
  typeof define === 'function' && define.amd ? define(['jquery'], factory) :
  (factory(global.jQuery));
}(this, (function ($) { 'use strict';

  $ = $ && $.hasOwnProperty('default') ? $['default'] : $;

  var IN_BROWSER = typeof window !== 'undefined';
  var WINDOW = IN_BROWSER ? window : {};
  var NAMESPACE = 'cropper';

  // Actions
  var ACTION_ALL = 'all';
  var ACTION_CROP = 'crop';
  var ACTION_MOVE = 'move';
  var ACTION_ZOOM = 'zoom';
  var ACTION_EAST = 'e';
  var ACTION_WEST = 'w';
  var ACTION_SOUTH = 's';
  var ACTION_NORTH = 'n';
  var ACTION_NORTH_EAST = 'ne';
  var ACTION_NORTH_WEST = 'nw';
  var ACTION_SOUTH_EAST = 'se';
  var ACTION_SOUTH_WEST = 'sw';

  // Classes
  var CLASS_CROP = NAMESPACE + '-crop';
  var CLASS_DISABLED = NAMESPACE + '-disabled';
  var CLASS_HIDDEN = NAMESPACE + '-hidden';
  var CLASS_HIDE = NAMESPACE + '-hide';
  var CLASS_INVISIBLE = NAMESPACE + '-invisible';
  var CLASS_MODAL = NAMESPACE + '-modal';
  var CLASS_MOVE = NAMESPACE + '-move';

  // Data keys
  var DATA_ACTION = 'action';
  var DATA_PREVIEW = 'preview';

  // Drag modes
  var DRAG_MODE_CROP = 'crop';
  var DRAG_MODE_MOVE = 'move';
  var DRAG_MODE_NONE = 'none';

  // Events
  var EVENT_CROP = 'crop';
  var EVENT_CROP_END = 'cropend';
  var EVENT_CROP_MOVE = 'cropmove';
  var EVENT_CROP_START = 'cropstart';
  var EVENT_DBLCLICK = 'dblclick';
  var EVENT_LOAD = 'load';

  // var EVENT_POINTER_DOWN = WINDOW.PointerEvent ? 'pointerdown' : 'touchstart mousedown';
  // var EVENT_POINTER_MOVE = WINDOW.PointerEvent ? 'pointermove' : 'touchmove mousemove';
  // var EVENT_POINTER_UP = WINDOW.PointerEvent ? 'pointerup pointercancel' : 'touchend touchcancel mouseup';

  var EVENT_POINTER_DOWN = 0 ? 'pointerdown' : 'touchstart mousedown';
  var EVENT_POINTER_MOVE = 0 ? 'pointermove' : 'touchmove mousemove';
  var EVENT_POINTER_UP = 0 ? 'pointerup pointercancel' : 'touchend touchcancel mouseup';
  
  var EVENT_READY = 'ready';
  var EVENT_RESIZE = 'resize';
  var EVENT_WHEEL = 'wheel mousewheel DOMMouseScroll';
  var EVENT_ZOOM = 'zoom';

  // RegExps
  var REGEXP_ACTIONS = /^(?:e|w|s|n|se|sw|ne|nw|all|crop|move|zoom)$/;
  var REGEXP_DATA_URL = /^data:/;
  var REGEXP_DATA_URL_JPEG = /^data:image\/jpeg;base64,/;
  var REGEXP_TAG_NAME = /^(?:img|canvas)$/i;

  var DEFAULTS = {
    // Define the view mode of the cropper
    viewMode: 0, // 0, 1, 2, 3

    // Define the dragging mode of the cropper
    dragMode: DRAG_MODE_CROP, // 'crop', 'move' or 'none'

    // Define the aspect ratio of the crop box
    aspectRatio: NaN,

    // An object with the previous cropping result data
    data: null,

    // A selector for adding extra containers to preview
    preview: '',

    // Re-render the cropper when resize the window
    responsive: true,

    // Restore the cropped area after resize the window
    restore: true,

    // Check if the current image is a cross-origin image
    checkCrossOrigin: true,

    // Check the current image's Exif Orientation information
    checkOrientation: true,

    // Show the black modal
    modal: true,

    // Show the dashed lines for guiding
    guides: true,

    // Show the center indicator for guiding
    center: true,

    // Show the white modal to highlight the crop box
    highlight: true,

    // Show the grid background
    background: true,

    // Enable to crop the image automatically when initialize
    autoCrop: true,

    // Define the percentage of automatic cropping area when initializes
    autoCropArea: 0.8,

    // Enable to move the image
    movable: true,

    // Enable to rotate the image
    rotatable: true,

    // Enable to scale the image
    scalable: true,

    // Enable to zoom the image
    zoomable: true,

    // Enable to zoom the image by dragging touch
    zoomOnTouch: true,

    // Enable to zoom the image by wheeling mouse
    zoomOnWheel: true,

    // Define zoom ratio when zoom the image by wheeling mouse
    wheelZoomRatio: 0.1,

    // Enable to move the crop box
    cropBoxMovable: true,

    // Enable to resize the crop box
    cropBoxResizable: true,

    // Toggle drag mode between "crop" and "move" when click twice on the cropper
    toggleDragModeOnDblclick: true,

    // Size limitation
    minCanvasWidth: 0,
    minCanvasHeight: 0,
    minCropBoxWidth: 0,
    minCropBoxHeight: 0,
    minContainerWidth: 200,
    minContainerHeight: 100,

    // Shortcuts of events
    ready: null,
    cropstart: null,
    cropmove: null,
    cropend: null,
    crop: null,
    zoom: null
  };

  var TEMPLATE = '<div class="cropper-container" touch-action="none">' + '<div class="cropper-wrap-box">' + '<div class="cropper-canvas"></div>' + '</div>' + '<div class="cropper-drag-box"></div>' + '<div class="cropper-crop-box">' + '<span class="cropper-view-box"></span>' + '<span class="cropper-dashed dashed-h"></span>' + '<span class="cropper-dashed dashed-v"></span>' + '<span class="cropper-center"></span>' + '<span class="cropper-face"></span>' + '<span class="cropper-line line-e" data-action="e"></span>' + '<span class="cropper-line line-n" data-action="n"></span>' + '<span class="cropper-line line-w" data-action="w"></span>' + '<span class="cropper-line line-s" data-action="s"></span>' + '<span class="cropper-point point-e" data-action="e"></span>' + '<span class="cropper-point point-n" data-action="n"></span>' + '<span class="cropper-point point-w" data-action="w"></span>' + '<span class="cropper-point point-s" data-action="s"></span>' + '<span class="cropper-point point-ne" data-action="ne"></span>' + '<span class="cropper-point point-nw" data-action="nw"></span>' + '<span class="cropper-point point-sw" data-action="sw"></span>' + '<span class="cropper-point point-se" data-action="se"></span>' + '</div>' + '</div>';

  var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
    return typeof obj;
  } : function (obj) {
    return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
  };

  var classCallCheck = function (instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  };

  var createClass = function () {
    function defineProperties(target, props) {
      for (var i = 0; i < props.length; i++) {
        var descriptor = props[i];
        descriptor.enumerable = descriptor.enumerable || false;
        descriptor.configurable = true;
        if ("value" in descriptor) descriptor.writable = true;
        Object.defineProperty(target, descriptor.key, descriptor);
      }
    }

    return function (Constructor, protoProps, staticProps) {
      if (protoProps) defineProperties(Constructor.prototype, protoProps);
      if (staticProps) defineProperties(Constructor, staticProps);
      return Constructor;
    };
  }();

  var toConsumableArray = function (arr) {
    if (Array.isArray(arr)) {
      for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) arr2[i] = arr[i];

      return arr2;
    } else {
      return Array.from(arr);
    }
  };

  /**
   * Check if the given value is not a number.
   */
  var isNaN = Number.isNaN || WINDOW.isNaN;

  /**
   * Check if the given value is a number.
   * @param {*} value - The value to check.
   * @returns {boolean} Returns `true` if the given value is a number, else `false`.
   */
  function isNumber(value) {
    return typeof value === 'number' && !isNaN(value);
  }

  /**
   * Check if the given value is undefined.
   * @param {*} value - The value to check.
   * @returns {boolean} Returns `true` if the given value is undefined, else `false`.
   */
  function isUndefined(value) {
    return typeof value === 'undefined';
  }

  /**
   * Check if the given value is an object.
   * @param {*} value - The value to check.
   * @returns {boolean} Returns `true` if the given value is an object, else `false`.
   */
  function isObject(value) {
    return (typeof value === 'undefined' ? 'undefined' : _typeof(value)) === 'object' && value !== null;
  }

  var hasOwnProperty = Object.prototype.hasOwnProperty;

  /**
   * Check if the given value is a plain object.
   * @param {*} value - The value to check.
   * @returns {boolean} Returns `true` if the given value is a plain object, else `false`.
   */

  function isPlainObject(value) {
    if (!isObject(value)) {
      return false;
    }

    try {
      var _constructor = value.constructor;
      var prototype = _constructor.prototype;


      return _constructor && prototype && hasOwnProperty.call(prototype, 'isPrototypeOf');
    } catch (e) {
      return false;
    }
  }

  /**
   * Check if the given value is a function.
   * @param {*} value - The value to check.
   * @returns {boolean} Returns `true` if the given value is a function, else `false`.
   */
  function isFunction(value) {
    return typeof value === 'function';
  }

  /**
   * Iterate the given data.
   * @param {*} data - The data to iterate.
   * @param {Function} callback - The process function for each element.
   * @returns {*} The original data.
   */
  function forEach(data, callback) {
    if (data && isFunction(callback)) {
      if (Array.isArray(data) || isNumber(data.length) /* array-like */) {
          var length = data.length;

          var i = void 0;

          for (i = 0; i < length; i += 1) {
            if (callback.call(data, data[i], i, data) === false) {
              break;
            }
          }
        } else if (isObject(data)) {
        Object.keys(data).forEach(function (key) {
          callback.call(data, data[key], key, data);
        });
      }
    }

    return data;
  }

  /**
   * Extend the given object.
   * @param {*} obj - The object to be extended.
   * @param {*} args - The rest objects which will be merged to the first object.
   * @returns {Object} The extended object.
   */
  var assign = Object.assign || function assign(obj) {
    for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
      args[_key - 1] = arguments[_key];
    }

    if (isObject(obj) && args.length > 0) {
      args.forEach(function (arg) {
        if (isObject(arg)) {
          Object.keys(arg).forEach(function (key) {
            obj[key] = arg[key];
          });
        }
      });
    }

    return obj;
  };

  var REGEXP_DECIMALS = /\.\d*(?:0|9){12}\d*$/i;

  /**
   * Normalize decimal number.
   * Check out {@link http://0.30000000000000004.com/}
   * @param {number} value - The value to normalize.
   * @param {number} [times=100000000000] - The times for normalizing.
   * @returns {number} Returns the normalized number.
   */
  function normalizeDecimalNumber(value) {
    var times = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 100000000000;

    return REGEXP_DECIMALS.test(value) ? Math.round(value * times) / times : value;
  }

  var REGEXP_SUFFIX = /^(?:width|height|left|top|marginLeft|marginTop)$/;

  /**
   * Apply styles to the given element.
   * @param {Element} element - The target element.
   * @param {Object} styles - The styles for applying.
   */
  function setStyle(element, styles) {
    var style = element.style;


    forEach(styles, function (value, property) {
      if (REGEXP_SUFFIX.test(property) && isNumber(value)) {
        value += 'px';
      }

      style[property] = value;
    });
  }

  /**
   * Check if the given element has a special class.
   * @param {Element} element - The element to check.
   * @param {string} value - The class to search.
   * @returns {boolean} Returns `true` if the special class was found.
   */
  function hasClass(element, value) {
    return element.classList ? element.classList.contains(value) : element.className.indexOf(value) > -1;
  }

  /**
   * Add classes to the given element.
   * @param {Element} element - The target element.
   * @param {string} value - The classes to be added.
   */
  function addClass(element, value) {
    if (!value) {
      return;
    }

    if (isNumber(element.length)) {
      forEach(element, function (elem) {
        addClass(elem, value);
      });
      return;
    }

    if (element.classList) {
      element.classList.add(value);
      return;
    }

    var className = element.className.trim();

    if (!className) {
      element.className = value;
    } else if (className.indexOf(value) < 0) {
      element.className = className + ' ' + value;
    }
  }

  /**
   * Remove classes from the given element.
   * @param {Element} element - The target element.
   * @param {string} value - The classes to be removed.
   */
  function removeClass(element, value) {
    if (!value) {
      return;
    }

    if (isNumber(element.length)) {
      forEach(element, function (elem) {
        removeClass(elem, value);
      });
      return;
    }

    if (element.classList) {
      element.classList.remove(value);
      return;
    }

    if (element.className.indexOf(value) >= 0) {
      element.className = element.className.replace(value, '');
    }
  }

  /**
   * Add or remove classes from the given element.
   * @param {Element} element - The target element.
   * @param {string} value - The classes to be toggled.
   * @param {boolean} added - Add only.
   */
  function toggleClass(element, value, added) {
    if (!value) {
      return;
    }

    if (isNumber(element.length)) {
      forEach(element, function (elem) {
        toggleClass(elem, value, added);
      });
      return;
    }

    // IE10-11 doesn't support the second parameter of `classList.toggle`
    if (added) {
      addClass(element, value);
    } else {
      removeClass(element, value);
    }
  }

  var REGEXP_HYPHENATE = /([a-z\d])([A-Z])/g;

  /**
   * Transform the given string from camelCase to kebab-case
   * @param {string} value - The value to transform.
   * @returns {string} The transformed value.
   */
  function hyphenate(value) {
    return value.replace(REGEXP_HYPHENATE, '$1-$2').toLowerCase();
  }

  /**
   * Get data from the given element.
   * @param {Element} element - The target element.
   * @param {string} name - The data key to get.
   * @returns {string} The data value.
   */
  function getData(element, name) {
    if (isObject(element[name])) {
      return element[name];
    } else if (element.dataset) {
      return element.dataset[name];
    }

    return element.getAttribute('data-' + hyphenate(name));
  }

  /**
   * Set data to the given element.
   * @param {Element} element - The target element.
   * @param {string} name - The data key to set.
   * @param {string} data - The data value.
   */
  function setData(element, name, data) {
    if (isObject(data)) {
      element[name] = data;
    } else if (element.dataset) {
      element.dataset[name] = data;
    } else {
      element.setAttribute('data-' + hyphenate(name), data);
    }
  }

  /**
   * Remove data from the given element.
   * @param {Element} element - The target element.
   * @param {string} name - The data key to remove.
   */
  function removeData(element, name) {
    if (isObject(element[name])) {
      try {
        delete element[name];
      } catch (e) {
        element[name] = undefined;
      }
    } else if (element.dataset) {
      // #128 Safari not allows to delete dataset property
      try {
        delete element.dataset[name];
      } catch (e) {
        element.dataset[name] = undefined;
      }
    } else {
      element.removeAttribute('data-' + hyphenate(name));
    }
  }

  var REGEXP_SPACES = /\s\s*/;
  var onceSupported = function () {
    var supported = false;

    if (IN_BROWSER) {
      var once = false;
      var listener = function listener() {};
      var options = Object.defineProperty({}, 'once', {
        get: function get$$1() {
          supported = true;
          return once;
        },


        /**
         * This setter can fix a `TypeError` in strict mode
         * {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Errors/Getter_only}
         * @param {boolean} value - The value to set
         */
        set: function set$$1(value) {
          once = value;
        }
      });

      WINDOW.addEventListener('test', listener, options);
      WINDOW.removeEventListener('test', listener, options);
    }

    return supported;
  }();

  /**
   * Remove event listener from the target element.
   * @param {Element} element - The event target.
   * @param {string} type - The event type(s).
   * @param {Function} listener - The event listener.
   * @param {Object} options - The event options.
   */
  function removeListener(element, type, listener) {
    var options = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : {};

    var handler = listener;

    type.trim().split(REGEXP_SPACES).forEach(function (event) {
      if (!onceSupported) {
        var listeners = element.listeners;


        if (listeners && listeners[event] && listeners[event][listener]) {
          handler = listeners[event][listener];
          delete listeners[event][listener];

          if (Object.keys(listeners[event]).length === 0) {
            delete listeners[event];
          }

          if (Object.keys(listeners).length === 0) {
            delete element.listeners;
          }
        }
      }

      element.removeEventListener(event, handler, options);
    });
  }

  /**
   * Add event listener to the target element.
   * @param {Element} element - The event target.
   * @param {string} type - The event type(s).
   * @param {Function} listener - The event listener.
   * @param {Object} options - The event options.
   */
  function addListener(element, type, listener) {
    var options = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : {};

    var _handler = listener;

    type.trim().split(REGEXP_SPACES).forEach(function (event) {
      if (options.once && !onceSupported) {
        var _element$listeners = element.listeners,
            listeners = _element$listeners === undefined ? {} : _element$listeners;


        _handler = function handler() {
          for (var _len2 = arguments.length, args = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
            args[_key2] = arguments[_key2];
          }

          delete listeners[event][listener];
          element.removeEventListener(event, _handler, options);
          listener.apply(element, args);
        };

        if (!listeners[event]) {
          listeners[event] = {};
        }

        if (listeners[event][listener]) {
          element.removeEventListener(event, listeners[event][listener], options);
        }

        listeners[event][listener] = _handler;
        element.listeners = listeners;
      }

      element.addEventListener(event, _handler, options);
    });
  }

  /**
   * Dispatch event on the target element.
   * @param {Element} element - The event target.
   * @param {string} type - The event type(s).
   * @param {Object} data - The additional event data.
   * @returns {boolean} Indicate if the event is default prevented or not.
   */
  function dispatchEvent(element, type, data) {
    var event = void 0;

    // Event and CustomEvent on IE9-11 are global objects, not constructors
    if (isFunction(Event) && isFunction(CustomEvent)) {
      event = new CustomEvent(type, {
        detail: data,
        bubbles: true,
        cancelable: true
      });
    } else {
      event = document.createEvent('CustomEvent');
      event.initCustomEvent(type, true, true, data);
    }

    return element.dispatchEvent(event);
  }

  /**
   * Get the offset base on the document.
   * @param {Element} element - The target element.
   * @returns {Object} The offset data.
   */
  function getOffset(element) {
    var box = element.getBoundingClientRect();

    return {
      left: box.left + (window.pageXOffset - document.documentElement.clientLeft),
      top: box.top + (window.pageYOffset - document.documentElement.clientTop)
    };
  }

  var location = WINDOW.location;

  var REGEXP_ORIGINS = /^(https?:)\/\/([^:/?#]+):?(\d*)/i;

  /**
   * Check if the given URL is a cross origin URL.
   * @param {string} url - The target URL.
   * @returns {boolean} Returns `true` if the given URL is a cross origin URL, else `false`.
   */
  function isCrossOriginURL(url) {
    var parts = url.match(REGEXP_ORIGINS);

    return parts && (parts[1] !== location.protocol || parts[2] !== location.hostname || parts[3] !== location.port);
  }

  /**
   * Add timestamp to the given URL.
   * @param {string} url - The target URL.
   * @returns {string} The result URL.
   */
  function addTimestamp(url) {
    var timestamp = 'timestamp=' + new Date().getTime();

    return url + (url.indexOf('?') === -1 ? '?' : '&') + timestamp;
  }

  /**
   * Get transforms base on the given object.
   * @param {Object} obj - The target object.
   * @returns {string} A string contains transform values.
   */
  function getTransforms(_ref) {
    var rotate = _ref.rotate,
        scaleX = _ref.scaleX,
        scaleY = _ref.scaleY,
        translateX = _ref.translateX,
        translateY = _ref.translateY;

    var values = [];

    if (isNumber(translateX) && translateX !== 0) {
      values.push('translateX(' + translateX + 'px)');
    }

    if (isNumber(translateY) && translateY !== 0) {
      values.push('translateY(' + translateY + 'px)');
    }

    // Rotate should come first before scale to match orientation transform
    if (isNumber(rotate) && rotate !== 0) {
      values.push('rotate(' + rotate + 'deg)');
    }

    if (isNumber(scaleX) && scaleX !== 1) {
      values.push('scaleX(' + scaleX + ')');
    }

    if (isNumber(scaleY) && scaleY !== 1) {
      values.push('scaleY(' + scaleY + ')');
    }

    var transform = values.length ? values.join(' ') : 'none';

    return {
      WebkitTransform: transform,
      msTransform: transform,
      transform: transform
    };
  }

  /**
   * Get the max ratio of a group of pointers.
   * @param {string} pointers - The target pointers.
   * @returns {number} The result ratio.
   */
  function getMaxZoomRatio(pointers) {
    var pointers2 = assign({}, pointers);
    var ratios = [];

    forEach(pointers, function (pointer, pointerId) {
      delete pointers2[pointerId];

      forEach(pointers2, function (pointer2) {
        var x1 = Math.abs(pointer.startX - pointer2.startX);
        var y1 = Math.abs(pointer.startY - pointer2.startY);
        var x2 = Math.abs(pointer.endX - pointer2.endX);
        var y2 = Math.abs(pointer.endY - pointer2.endY);
        var z1 = Math.sqrt(x1 * x1 + y1 * y1);
        var z2 = Math.sqrt(x2 * x2 + y2 * y2);
        var ratio = (z2 - z1) / z1;

        ratios.push(ratio);
      });
    });

    ratios.sort(function (a, b) {
      return Math.abs(a) < Math.abs(b);
    });

    return ratios[0];
  }

  /**
   * Get a pointer from an event object.
   * @param {Object} event - The target event object.
   * @param {boolean} endOnly - Indicates if only returns the end point coordinate or not.
   * @returns {Object} The result pointer contains start and/or end point coordinates.
   */
  function getPointer(_ref2, endOnly) {
    var pageX = _ref2.pageX || _ref2.originalEvent.pageX,
        pageY = _ref2.pageY || _ref2.originalEvent.pageY;

    var end = {
      endX: pageX,
      endY: pageY
    };

    return endOnly ? end : assign({
      startX: pageX,
      startY: pageY
    }, end);
  }

  /**
   * Get the center point coordinate of a group of pointers.
   * @param {Object} pointers - The target pointers.
   * @returns {Object} The center point coordinate.
   */
  function getPointersCenter(pointers) {
    var pageX = 0;
    var pageY = 0;
    var count = 0;

    forEach(pointers, function (_ref3) {
      var startX = _ref3.startX,
          startY = _ref3.startY;

      pageX += startX;
      pageY += startY;
      count += 1;
    });

    pageX /= count;
    pageY /= count;

    return {
      pageX: pageX,
      pageY: pageY
    };
  }

  /**
   * Check if the given value is a finite number.
   */
  var isFinite = Number.isFinite || WINDOW.isFinite;

  /**
   * Get the max sizes in a rectangle under the given aspect ratio.
   * @param {Object} data - The original sizes.
   * @param {string} [type='contain'] - The adjust type.
   * @returns {Object} The result sizes.
   */
  function getAdjustedSizes(_ref4) // or 'cover'
  {
    var aspectRatio = _ref4.aspectRatio,
        height = _ref4.height,
        width = _ref4.width;
    var type = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 'contain';

    var isValidNumber = function isValidNumber(value) {
      return isFinite(value) && value > 0;
    };

    if (isValidNumber(width) && isValidNumber(height)) {
      var adjustedWidth = height * aspectRatio;

      if (type === 'contain' && adjustedWidth > width || type === 'cover' && adjustedWidth < width) {
        height = width / aspectRatio;
      } else {
        width = height * aspectRatio;
      }
    } else if (isValidNumber(width)) {
      height = width / aspectRatio;
    } else if (isValidNumber(height)) {
      width = height * aspectRatio;
    }

    return {
      width: width,
      height: height
    };
  }

  /**
   * Get the new sizes of a rectangle after rotated.
   * @param {Object} data - The original sizes.
   * @returns {Object} The result sizes.
   */
  function getRotatedSizes(_ref5) {
    var width = _ref5.width,
        height = _ref5.height,
        degree = _ref5.degree;

    degree = Math.abs(degree) % 180;

    if (degree === 90) {
      return {
        width: height,
        height: width
      };
    }

    var arc = degree % 90 * Math.PI / 180;
    var sinArc = Math.sin(arc);
    var cosArc = Math.cos(arc);
    var newWidth = width * cosArc + height * sinArc;
    var newHeight = width * sinArc + height * cosArc;

    return degree > 90 ? {
      width: newHeight,
      height: newWidth
    } : {
      width: newWidth,
      height: newHeight
    };
  }

  /**
   * Get a canvas which drew the given image.
   * @param {HTMLImageElement} image - The image for drawing.
   * @param {Object} imageData - The image data.
   * @param {Object} canvasData - The canvas data.
   * @param {Object} options - The options.
   * @returns {HTMLCanvasElement} The result canvas.
   */
  function getSourceCanvas(image, _ref6, _ref7, _ref8) {
    var imageAspectRatio = _ref6.aspectRatio,
        imageNaturalWidth = _ref6.naturalWidth,
        imageNaturalHeight = _ref6.naturalHeight,
        _ref6$rotate = _ref6.rotate,
        rotate = _ref6$rotate === undefined ? 0 : _ref6$rotate,
        _ref6$scaleX = _ref6.scaleX,
        scaleX = _ref6$scaleX === undefined ? 1 : _ref6$scaleX,
        _ref6$scaleY = _ref6.scaleY,
        scaleY = _ref6$scaleY === undefined ? 1 : _ref6$scaleY;
    var aspectRatio = _ref7.aspectRatio,
        naturalWidth = _ref7.naturalWidth,
        naturalHeight = _ref7.naturalHeight;
    var _ref8$fillColor = _ref8.fillColor,
        fillColor = _ref8$fillColor === undefined ? 'transparent' : _ref8$fillColor,
        _ref8$imageSmoothingE = _ref8.imageSmoothingEnabled,
        imageSmoothingEnabled = _ref8$imageSmoothingE === undefined ? true : _ref8$imageSmoothingE,
        _ref8$imageSmoothingQ = _ref8.imageSmoothingQuality,
        imageSmoothingQuality = _ref8$imageSmoothingQ === undefined ? 'low' : _ref8$imageSmoothingQ,
        _ref8$maxWidth = _ref8.maxWidth,
        maxWidth = _ref8$maxWidth === undefined ? Infinity : _ref8$maxWidth,
        _ref8$maxHeight = _ref8.maxHeight,
        maxHeight = _ref8$maxHeight === undefined ? Infinity : _ref8$maxHeight,
        _ref8$minWidth = _ref8.minWidth,
        minWidth = _ref8$minWidth === undefined ? 0 : _ref8$minWidth,
        _ref8$minHeight = _ref8.minHeight,
        minHeight = _ref8$minHeight === undefined ? 0 : _ref8$minHeight;

    var canvas = document.createElement('canvas');
    var context = canvas.getContext('2d');
    var maxSizes = getAdjustedSizes({
      aspectRatio: aspectRatio,
      width: maxWidth,
      height: maxHeight
    });
    var minSizes = getAdjustedSizes({
      aspectRatio: aspectRatio,
      width: minWidth,
      height: minHeight
    }, 'cover');
    var width = Math.min(maxSizes.width, Math.max(minSizes.width, naturalWidth));
    var height = Math.min(maxSizes.height, Math.max(minSizes.height, naturalHeight));

    // Note: should always use image's natural sizes for drawing as
    // imageData.naturalWidth === canvasData.naturalHeight when rotate % 180 === 90
    var destMaxSizes = getAdjustedSizes({
      aspectRatio: imageAspectRatio,
      width: maxWidth,
      height: maxHeight
    });
    var destMinSizes = getAdjustedSizes({
      aspectRatio: imageAspectRatio,
      width: minWidth,
      height: minHeight
    }, 'cover');
    var destWidth = Math.min(destMaxSizes.width, Math.max(destMinSizes.width, imageNaturalWidth));
    var destHeight = Math.min(destMaxSizes.height, Math.max(destMinSizes.height, imageNaturalHeight));
    var params = [-destWidth / 2, -destHeight / 2, destWidth, destHeight];

    canvas.width = normalizeDecimalNumber(width);
    canvas.height = normalizeDecimalNumber(height);
    context.fillStyle = fillColor;
    context.fillRect(0, 0, width, height);
    context.save();
    context.translate(width / 2, height / 2);
    context.rotate(rotate * Math.PI / 180);
    context.scale(scaleX, scaleY);
    context.imageSmoothingEnabled = imageSmoothingEnabled;
    context.imageSmoothingQuality = imageSmoothingQuality;
    context.drawImage.apply(context, [image].concat(toConsumableArray(params.map(function (param) {
      return Math.floor(normalizeDecimalNumber(param));
    }))));
    context.restore();
    return canvas;
  }

  var fromCharCode = String.fromCharCode;

  /**
   * Get string from char code in data view.
   * @param {DataView} dataView - The data view for read.
   * @param {number} start - The start index.
   * @param {number} length - The read length.
   * @returns {string} The read result.
   */

  function getStringFromCharCode(dataView, start, length) {
    var str = '';
    var i = void 0;

    length += start;

    for (i = start; i < length; i += 1) {
      str += fromCharCode(dataView.getUint8(i));
    }

    return str;
  }

  var REGEXP_DATA_URL_HEAD = /^data:.*,/;

  /**
   * Transform Data URL to array buffer.
   * @param {string} dataURL - The Data URL to transform.
   * @returns {ArrayBuffer} The result array buffer.
   */
  function dataURLToArrayBuffer(dataURL) {
    var base64 = dataURL.replace(REGEXP_DATA_URL_HEAD, '');
    var binary = atob(base64);
    var arrayBuffer = new ArrayBuffer(binary.length);
    var uint8 = new Uint8Array(arrayBuffer);

    forEach(uint8, function (value, i) {
      uint8[i] = binary.charCodeAt(i);
    });

    return arrayBuffer;
  }

  /**
   * Transform array buffer to Data URL.
   * @param {ArrayBuffer} arrayBuffer - The array buffer to transform.
   * @param {string} mimeType - The mime type of the Data URL.
   * @returns {string} The result Data URL.
   */
  function arrayBufferToDataURL(arrayBuffer, mimeType) {
    var uint8 = new Uint8Array(arrayBuffer);
    var data = '';

    // TypedArray.prototype.forEach is not supported in some browsers.
    forEach(uint8, function (value) {
      data += fromCharCode(value);
    });

    return 'data:' + mimeType + ';base64,' + btoa(data);
  }

  /**
   * Get orientation value from given array buffer.
   * @param {ArrayBuffer} arrayBuffer - The array buffer to read.
   * @returns {number} The read orientation value.
   */
  function getOrientation(arrayBuffer) {
    var dataView = new DataView(arrayBuffer);
    var orientation = void 0;
    var littleEndian = void 0;
    var app1Start = void 0;
    var ifdStart = void 0;

    // Only handle JPEG image (start by 0xFFD8)
    if (dataView.getUint8(0) === 0xFF && dataView.getUint8(1) === 0xD8) {
      var length = dataView.byteLength;
      var offset = 2;

      while (offset < length) {
        if (dataView.getUint8(offset) === 0xFF && dataView.getUint8(offset + 1) === 0xE1) {
          app1Start = offset;
          break;
        }

        offset += 1;
      }
    }

    if (app1Start) {
      var exifIDCode = app1Start + 4;
      var tiffOffset = app1Start + 10;

      if (getStringFromCharCode(dataView, exifIDCode, 4) === 'Exif') {
        var endianness = dataView.getUint16(tiffOffset);

        littleEndian = endianness === 0x4949;

        if (littleEndian || endianness === 0x4D4D /* bigEndian */) {
            if (dataView.getUint16(tiffOffset + 2, littleEndian) === 0x002A) {
              var firstIFDOffset = dataView.getUint32(tiffOffset + 4, littleEndian);

              if (firstIFDOffset >= 0x00000008) {
                ifdStart = tiffOffset + firstIFDOffset;
              }
            }
          }
      }
    }

    if (ifdStart) {
      var _length = dataView.getUint16(ifdStart, littleEndian);
      var _offset = void 0;
      var i = void 0;

      for (i = 0; i < _length; i += 1) {
        _offset = ifdStart + i * 12 + 2;

        if (dataView.getUint16(_offset, littleEndian) === 0x0112 /* Orientation */) {
            // 8 is the offset of the current tag's value
            _offset += 8;

            // Get the original orientation value
            orientation = dataView.getUint16(_offset, littleEndian);

            // Override the orientation with its default value
            dataView.setUint16(_offset, 1, littleEndian);
            break;
          }
      }
    }

    return orientation;
  }

  /**
   * Parse Exif Orientation value.
   * @param {number} orientation - The orientation to parse.
   * @returns {Object} The parsed result.
   */
  function parseOrientation(orientation) {
    var rotate = 0;
    var scaleX = 1;
    var scaleY = 1;

    switch (orientation) {
      // Flip horizontal
      case 2:
        scaleX = -1;
        break;

      // Rotate left 180°
      case 3:
        rotate = -180;
        break;

      // Flip vertical
      case 4:
        scaleY = -1;
        break;

      // Flip vertical and rotate right 90°
      case 5:
        rotate = 90;
        scaleY = -1;
        break;

      // Rotate right 90°
      case 6:
        rotate = 90;
        break;

      // Flip horizontal and rotate right 90°
      case 7:
        rotate = 90;
        scaleX = -1;
        break;

      // Rotate left 90°
      case 8:
        rotate = -90;
        break;

      default:
    }

    return {
      rotate: rotate,
      scaleX: scaleX,
      scaleY: scaleY
    };
  }

  var render = {
    render: function render() {
      this.initContainer();
      this.initCanvas();
      this.initCropBox();
      this.renderCanvas();

      if (this.cropped) {
        this.renderCropBox();
      }
    },
    initContainer: function initContainer() {
      var element = this.element,
          options = this.options,
          container = this.container,
          cropper = this.cropper;

      addClass(cropper, CLASS_HIDDEN);
      removeClass(element, CLASS_HIDDEN);

      var containerData = {
        width: Math.max(container.offsetWidth, Number(options.minContainerWidth) || 200),
        height: Math.max(container.offsetHeight, Number(options.minContainerHeight) || 100)
      };

      this.containerData = containerData;

      setStyle(cropper, {
        width: containerData.width,
        height: containerData.height
      });

      addClass(element, CLASS_HIDDEN);
      removeClass(cropper, CLASS_HIDDEN);
    },


    // Canvas (image wrapper)
    initCanvas: function initCanvas() {
      var containerData = this.containerData,
          imageData = this.imageData;
      var viewMode = this.options.viewMode;

      var rotated = Math.abs(imageData.rotate) % 180 === 90;
      var naturalWidth = rotated ? imageData.naturalHeight : imageData.naturalWidth;
      var naturalHeight = rotated ? imageData.naturalWidth : imageData.naturalHeight;
      var aspectRatio = naturalWidth / naturalHeight;
      var canvasWidth = containerData.width;
      var canvasHeight = containerData.height;

      if (containerData.height * aspectRatio > containerData.width) {
        if (viewMode === 3) {
          canvasWidth = containerData.height * aspectRatio;
        } else {
          canvasHeight = containerData.width / aspectRatio;
        }
      } else if (viewMode === 3) {
        canvasHeight = containerData.width / aspectRatio;
      } else {
        canvasWidth = containerData.height * aspectRatio;
      }

      var canvasData = {
        aspectRatio: aspectRatio,
        naturalWidth: naturalWidth,
        naturalHeight: naturalHeight,
        width: canvasWidth,
        height: canvasHeight
      };

      canvasData.left = (containerData.width - canvasWidth) / 2;
      canvasData.top = (containerData.height - canvasHeight) / 2;
      canvasData.oldLeft = canvasData.left;
      canvasData.oldTop = canvasData.top;

      this.canvasData = canvasData;
      this.limited = viewMode === 1 || viewMode === 2;
      this.limitCanvas(true, true);
      this.initialImageData = assign({}, imageData);
      this.initialCanvasData = assign({}, canvasData);
    },
    limitCanvas: function limitCanvas(sizeLimited, positionLimited) {
      var options = this.options,
          containerData = this.containerData,
          canvasData = this.canvasData,
          cropBoxData = this.cropBoxData;
      var viewMode = options.viewMode;
      var aspectRatio = canvasData.aspectRatio;

      var cropped = this.cropped && cropBoxData;

      if (sizeLimited) {
        var minCanvasWidth = Number(options.minCanvasWidth) || 0;
        var minCanvasHeight = Number(options.minCanvasHeight) || 0;

        if (viewMode > 1) {
          minCanvasWidth = Math.max(minCanvasWidth, containerData.width);
          minCanvasHeight = Math.max(minCanvasHeight, containerData.height);

          if (viewMode === 3) {
            if (minCanvasHeight * aspectRatio > minCanvasWidth) {
              minCanvasWidth = minCanvasHeight * aspectRatio;
            } else {
              minCanvasHeight = minCanvasWidth / aspectRatio;
            }
          }
        } else if (viewMode > 0) {
          if (minCanvasWidth) {
            minCanvasWidth = Math.max(minCanvasWidth, cropped ? cropBoxData.width : 0);
          } else if (minCanvasHeight) {
            minCanvasHeight = Math.max(minCanvasHeight, cropped ? cropBoxData.height : 0);
          } else if (cropped) {
            minCanvasWidth = cropBoxData.width;
            minCanvasHeight = cropBoxData.height;

            if (minCanvasHeight * aspectRatio > minCanvasWidth) {
              minCanvasWidth = minCanvasHeight * aspectRatio;
            } else {
              minCanvasHeight = minCanvasWidth / aspectRatio;
            }
          }
        }

        var _getAdjustedSizes = getAdjustedSizes({
          aspectRatio: aspectRatio,
          width: minCanvasWidth,
          height: minCanvasHeight
        });

        minCanvasWidth = _getAdjustedSizes.width;
        minCanvasHeight = _getAdjustedSizes.height;


        canvasData.minWidth = minCanvasWidth;
        canvasData.minHeight = minCanvasHeight;
        canvasData.maxWidth = Infinity;
        canvasData.maxHeight = Infinity;
      }

      if (positionLimited) {
        if (viewMode) {
          var newCanvasLeft = containerData.width - canvasData.width;
          var newCanvasTop = containerData.height - canvasData.height;

          canvasData.minLeft = Math.min(0, newCanvasLeft);
          canvasData.minTop = Math.min(0, newCanvasTop);
          canvasData.maxLeft = Math.max(0, newCanvasLeft);
          canvasData.maxTop = Math.max(0, newCanvasTop);

          if (cropped && this.limited) {
            canvasData.minLeft = Math.min(cropBoxData.left, cropBoxData.left + (cropBoxData.width - canvasData.width));
            canvasData.minTop = Math.min(cropBoxData.top, cropBoxData.top + (cropBoxData.height - canvasData.height));
            canvasData.maxLeft = cropBoxData.left;
            canvasData.maxTop = cropBoxData.top;

            if (viewMode === 2) {
              if (canvasData.width >= containerData.width) {
                canvasData.minLeft = Math.min(0, newCanvasLeft);
                canvasData.maxLeft = Math.max(0, newCanvasLeft);
              }

              if (canvasData.height >= containerData.height) {
                canvasData.minTop = Math.min(0, newCanvasTop);
                canvasData.maxTop = Math.max(0, newCanvasTop);
              }
            }
          }
        } else {
          canvasData.minLeft = -canvasData.width;
          canvasData.minTop = -canvasData.height;
          canvasData.maxLeft = containerData.width;
          canvasData.maxTop = containerData.height;
        }
      }
    },
    renderCanvas: function renderCanvas(changed, transformed) {
      var canvasData = this.canvasData,
          imageData = this.imageData;


      if (transformed) {
        var _getRotatedSizes = getRotatedSizes({
          width: imageData.naturalWidth * Math.abs(imageData.scaleX || 1),
          height: imageData.naturalHeight * Math.abs(imageData.scaleY || 1),
          degree: imageData.rotate || 0
        }),
            naturalWidth = _getRotatedSizes.width,
            naturalHeight = _getRotatedSizes.height;

        var width = canvasData.width * (naturalWidth / canvasData.naturalWidth);
        var height = canvasData.height * (naturalHeight / canvasData.naturalHeight);

        canvasData.left -= (width - canvasData.width) / 2;
        canvasData.top -= (height - canvasData.height) / 2;
        canvasData.width = width;
        canvasData.height = height;
        canvasData.aspectRatio = naturalWidth / naturalHeight;
        canvasData.naturalWidth = naturalWidth;
        canvasData.naturalHeight = naturalHeight;
        this.limitCanvas(true, false);
      }

      if (canvasData.width > canvasData.maxWidth || canvasData.width < canvasData.minWidth) {
        canvasData.left = canvasData.oldLeft;
      }

      if (canvasData.height > canvasData.maxHeight || canvasData.height < canvasData.minHeight) {
        canvasData.top = canvasData.oldTop;
      }

      canvasData.width = Math.min(Math.max(canvasData.width, canvasData.minWidth), canvasData.maxWidth);
      canvasData.height = Math.min(Math.max(canvasData.height, canvasData.minHeight), canvasData.maxHeight);

      this.limitCanvas(false, true);

      canvasData.left = Math.min(Math.max(canvasData.left, canvasData.minLeft), canvasData.maxLeft);
      canvasData.top = Math.min(Math.max(canvasData.top, canvasData.minTop), canvasData.maxTop);
      canvasData.oldLeft = canvasData.left;
      canvasData.oldTop = canvasData.top;

      setStyle(this.canvas, assign({
        width: canvasData.width,
        height: canvasData.height
      }, getTransforms({
        translateX: canvasData.left,
        translateY: canvasData.top
      })));

      this.renderImage(changed);

      if (this.cropped && this.limited) {
        this.limitCropBox(true, true);
      }
    },
    renderImage: function renderImage(changed) {
      var canvasData = this.canvasData,
          imageData = this.imageData;

      var width = imageData.naturalWidth * (canvasData.width / canvasData.naturalWidth);
      var height = imageData.naturalHeight * (canvasData.height / canvasData.naturalHeight);

      assign(imageData, {
        width: width,
        height: height,
        left: (canvasData.width - width) / 2,
        top: (canvasData.height - height) / 2
      });
      setStyle(this.image, assign({
        width: imageData.width,
        height: imageData.height
      }, getTransforms(assign({
        translateX: imageData.left,
        translateY: imageData.top
      }, imageData))));

      if (changed) {
        this.output();
      }
    },
    initCropBox: function initCropBox() {
      var options = this.options,
          canvasData = this.canvasData;
      var aspectRatio = options.aspectRatio;

      var autoCropArea = Number(options.autoCropArea) || 0.8;
      var cropBoxData = {
        width: canvasData.width,
        height: canvasData.height
      };

      if (aspectRatio) {
        if (canvasData.height * aspectRatio > canvasData.width) {
          cropBoxData.height = cropBoxData.width / aspectRatio;
        } else {
          cropBoxData.width = cropBoxData.height * aspectRatio;
        }
      }

      this.cropBoxData = cropBoxData;
      this.limitCropBox(true, true);

      // Initialize auto crop area
      cropBoxData.width = Math.min(Math.max(cropBoxData.width, cropBoxData.minWidth), cropBoxData.maxWidth);
      cropBoxData.height = Math.min(Math.max(cropBoxData.height, cropBoxData.minHeight), cropBoxData.maxHeight);

      // The width/height of auto crop area must large than "minWidth/Height"
      cropBoxData.width = Math.max(cropBoxData.minWidth, cropBoxData.width * autoCropArea);
      cropBoxData.height = Math.max(cropBoxData.minHeight, cropBoxData.height * autoCropArea);
      cropBoxData.left = canvasData.left + (canvasData.width - cropBoxData.width) / 2;
      cropBoxData.top = canvasData.top + (canvasData.height - cropBoxData.height) / 2;
      cropBoxData.oldLeft = cropBoxData.left;
      cropBoxData.oldTop = cropBoxData.top;

      this.initialCropBoxData = assign({}, cropBoxData);
    },
    limitCropBox: function limitCropBox(sizeLimited, positionLimited) {
      var options = this.options,
          containerData = this.containerData,
          canvasData = this.canvasData,
          cropBoxData = this.cropBoxData,
          limited = this.limited;
      var aspectRatio = options.aspectRatio;


      if (sizeLimited) {
        var minCropBoxWidth = Number(options.minCropBoxWidth) || 0;
        var minCropBoxHeight = Number(options.minCropBoxHeight) || 0;
        var maxCropBoxWidth = Math.min(containerData.width, limited ? canvasData.width : containerData.width);
        var maxCropBoxHeight = Math.min(containerData.height, limited ? canvasData.height : containerData.height);

        // The min/maxCropBoxWidth/Height must be less than container's width/height
        minCropBoxWidth = Math.min(minCropBoxWidth, containerData.width);
        minCropBoxHeight = Math.min(minCropBoxHeight, containerData.height);

        if (aspectRatio) {
          if (minCropBoxWidth && minCropBoxHeight) {
            if (minCropBoxHeight * aspectRatio > minCropBoxWidth) {
              minCropBoxHeight = minCropBoxWidth / aspectRatio;
            } else {
              minCropBoxWidth = minCropBoxHeight * aspectRatio;
            }
          } else if (minCropBoxWidth) {
            minCropBoxHeight = minCropBoxWidth / aspectRatio;
          } else if (minCropBoxHeight) {
            minCropBoxWidth = minCropBoxHeight * aspectRatio;
          }

          if (maxCropBoxHeight * aspectRatio > maxCropBoxWidth) {
            maxCropBoxHeight = maxCropBoxWidth / aspectRatio;
          } else {
            maxCropBoxWidth = maxCropBoxHeight * aspectRatio;
          }
        }

        // The minWidth/Height must be less than maxWidth/Height
        cropBoxData.minWidth = Math.min(minCropBoxWidth, maxCropBoxWidth);
        cropBoxData.minHeight = Math.min(minCropBoxHeight, maxCropBoxHeight);
        cropBoxData.maxWidth = maxCropBoxWidth;
        cropBoxData.maxHeight = maxCropBoxHeight;
      }

      if (positionLimited) {
        if (limited) {
          cropBoxData.minLeft = Math.max(0, canvasData.left);
          cropBoxData.minTop = Math.max(0, canvasData.top);
          cropBoxData.maxLeft = Math.min(containerData.width, canvasData.left + canvasData.width) - cropBoxData.width;
          cropBoxData.maxTop = Math.min(containerData.height, canvasData.top + canvasData.height) - cropBoxData.height;
        } else {
          cropBoxData.minLeft = 0;
          cropBoxData.minTop = 0;
          cropBoxData.maxLeft = containerData.width - cropBoxData.width;
          cropBoxData.maxTop = containerData.height - cropBoxData.height;
        }
      }
    },
    renderCropBox: function renderCropBox() {
      var options = this.options,
          containerData = this.containerData,
          cropBoxData = this.cropBoxData;


      if (cropBoxData.width > cropBoxData.maxWidth || cropBoxData.width < cropBoxData.minWidth) {
        cropBoxData.left = cropBoxData.oldLeft;
      }

      if (cropBoxData.height > cropBoxData.maxHeight || cropBoxData.height < cropBoxData.minHeight) {
        cropBoxData.top = cropBoxData.oldTop;
      }

      cropBoxData.width = Math.min(Math.max(cropBoxData.width, cropBoxData.minWidth), cropBoxData.maxWidth);
      cropBoxData.height = Math.min(Math.max(cropBoxData.height, cropBoxData.minHeight), cropBoxData.maxHeight);

      this.limitCropBox(false, true);

      cropBoxData.left = Math.min(Math.max(cropBoxData.left, cropBoxData.minLeft), cropBoxData.maxLeft);
      cropBoxData.top = Math.min(Math.max(cropBoxData.top, cropBoxData.minTop), cropBoxData.maxTop);
      cropBoxData.oldLeft = cropBoxData.left;
      cropBoxData.oldTop = cropBoxData.top;

      if (options.movable && options.cropBoxMovable) {
        // Turn to move the canvas when the crop box is equal to the container
        setData(this.face, DATA_ACTION, cropBoxData.width >= containerData.width && cropBoxData.height >= containerData.height ? ACTION_MOVE : ACTION_ALL);
      }

      setStyle(this.cropBox, assign({
        width: cropBoxData.width,
        height: cropBoxData.height
      }, getTransforms({
        translateX: cropBoxData.left,
        translateY: cropBoxData.top
      })));

      if (this.cropped && this.limited) {
        this.limitCanvas(true, true);
      }

      if (!this.disabled) {
        this.output();
      }
    },
    output: function output() {
      this.preview();
      dispatchEvent(this.element, EVENT_CROP, this.getData());
    }
  };

  var preview = {
    initPreview: function initPreview() {
      var crossOrigin = this.crossOrigin;
      var preview = this.options.preview;

      var url = crossOrigin ? this.crossOriginUrl : this.url;
      var image = document.createElement('img');

      if (crossOrigin) {
        image.crossOrigin = crossOrigin;
      }

      image.src = url;
      this.viewBox.appendChild(image);
      this.viewBoxImage = image;

      if (!preview) {
        return;
      }

      var previews = preview;

      if (typeof preview === 'string') {
        previews = this.element.ownerDocument.querySelectorAll(preview);
      } else if (preview.querySelector) {
        previews = [preview];
      }

      this.previews = previews;

      forEach(previews, function (el) {
        var img = document.createElement('img');

        // Save the original size for recover
        setData(el, DATA_PREVIEW, {
          width: el.offsetWidth,
          height: el.offsetHeight,
          html: el.innerHTML
        });

        if (crossOrigin) {
          img.crossOrigin = crossOrigin;
        }

        img.src = url;

        /**
         * Override img element styles
         * Add `display:block` to avoid margin top issue
         * Add `height:auto` to override `height` attribute on IE8
         * (Occur only when margin-top <= -height)
         */
        img.style.cssText = 'display:block;' + 'width:100%;' + 'height:auto;' + 'min-width:0!important;' + 'min-height:0!important;' + 'max-width:none!important;' + 'max-height:none!important;' + 'image-orientation:0deg!important;"';

        el.innerHTML = '';
        el.appendChild(img);
      });
    },
    resetPreview: function resetPreview() {
      forEach(this.previews, function (element) {
        var data = getData(element, DATA_PREVIEW);

        setStyle(element, {
          width: data.width,
          height: data.height
        });

        element.innerHTML = data.html;
        removeData(element, DATA_PREVIEW);
      });
    },
    preview: function preview() {
      var imageData = this.imageData,
          canvasData = this.canvasData,
          cropBoxData = this.cropBoxData;
      var cropBoxWidth = cropBoxData.width,
          cropBoxHeight = cropBoxData.height;
      var width = imageData.width,
          height = imageData.height;

      var left = cropBoxData.left - canvasData.left - imageData.left;
      var top = cropBoxData.top - canvasData.top - imageData.top;

      if (!this.cropped || this.disabled) {
        return;
      }

      setStyle(this.viewBoxImage, assign({
        width: width,
        height: height
      }, getTransforms(assign({
        translateX: -left,
        translateY: -top
      }, imageData))));

      forEach(this.previews, function (element) {
        var data = getData(element, DATA_PREVIEW);
        var originalWidth = data.width;
        var originalHeight = data.height;
        var newWidth = originalWidth;
        var newHeight = originalHeight;
        var ratio = 1;

        if (cropBoxWidth) {
          ratio = originalWidth / cropBoxWidth;
          newHeight = cropBoxHeight * ratio;
        }

        if (cropBoxHeight && newHeight > originalHeight) {
          ratio = originalHeight / cropBoxHeight;
          newWidth = cropBoxWidth * ratio;
          newHeight = originalHeight;
        }

        setStyle(element, {
          width: newWidth,
          height: newHeight
        });

        setStyle(element.getElementsByTagName('img')[0], assign({
          width: width * ratio,
          height: height * ratio
        }, getTransforms(assign({
          translateX: -left * ratio,
          translateY: -top * ratio
        }, imageData))));
      });
    }
  };

  var events = {
    bind: function bind() {
      var element = this.element,
          options = this.options,
          cropper = this.cropper;


      if (isFunction(options.cropstart)) {
        addListener(element, EVENT_CROP_START, options.cropstart);
      }

      if (isFunction(options.cropmove)) {
        addListener(element, EVENT_CROP_MOVE, options.cropmove);
      }

      if (isFunction(options.cropend)) {
        addListener(element, EVENT_CROP_END, options.cropend);
      }

      if (isFunction(options.crop)) {
        addListener(element, EVENT_CROP, options.crop);
      }

      if (isFunction(options.zoom)) {
        addListener(element, EVENT_ZOOM, options.zoom);
      }

      addListener(cropper, EVENT_POINTER_DOWN, this.onCropStart = this.cropStart.bind(this));

      if (options.zoomable && options.zoomOnWheel) {
        addListener(cropper, EVENT_WHEEL, this.onWheel = this.wheel.bind(this));
      }

      if (options.toggleDragModeOnDblclick) {
        addListener(cropper, EVENT_DBLCLICK, this.onDblclick = this.dblclick.bind(this));
      }

      addListener(element.ownerDocument, EVENT_POINTER_MOVE, this.onCropMove = this.cropMove.bind(this));
      addListener(element.ownerDocument, EVENT_POINTER_UP, this.onCropEnd = this.cropEnd.bind(this));

      if (options.responsive) {
        addListener(window, EVENT_RESIZE, this.onResize = this.resize.bind(this));
      }
    },
    unbind: function unbind() {
      var element = this.element,
          options = this.options,
          cropper = this.cropper;


      if (isFunction(options.cropstart)) {
        removeListener(element, EVENT_CROP_START, options.cropstart);
      }

      if (isFunction(options.cropmove)) {
        removeListener(element, EVENT_CROP_MOVE, options.cropmove);
      }

      if (isFunction(options.cropend)) {
        removeListener(element, EVENT_CROP_END, options.cropend);
      }

      if (isFunction(options.crop)) {
        removeListener(element, EVENT_CROP, options.crop);
      }

      if (isFunction(options.zoom)) {
        removeListener(element, EVENT_ZOOM, options.zoom);
      }

      removeListener(cropper, EVENT_POINTER_DOWN, this.onCropStart);

      if (options.zoomable && options.zoomOnWheel) {
        removeListener(cropper, EVENT_WHEEL, this.onWheel);
      }

      if (options.toggleDragModeOnDblclick) {
        removeListener(cropper, EVENT_DBLCLICK, this.onDblclick);
      }

      removeListener(element.ownerDocument, EVENT_POINTER_MOVE, this.onCropMove);
      removeListener(element.ownerDocument, EVENT_POINTER_UP, this.onCropEnd);

      if (options.responsive) {
        removeListener(window, EVENT_RESIZE, this.onResize);
      }
    }
  };

  var handlers = {
    resize: function resize() {
      var options = this.options,
          container = this.container,
          containerData = this.containerData;

      var minContainerWidth = Number(options.minContainerWidth) || 200;
      var minContainerHeight = Number(options.minContainerHeight) || 100;

      if (this.disabled || containerData.width <= minContainerWidth || containerData.height <= minContainerHeight) {
        return;
      }

      var ratio = container.offsetWidth / containerData.width;

      // Resize when width changed or height changed
      if (ratio !== 1 || container.offsetHeight !== containerData.height) {
        var canvasData = void 0;
        var cropBoxData = void 0;

        if (options.restore) {
          canvasData = this.getCanvasData();
          cropBoxData = this.getCropBoxData();
        }

        this.render();

        if (options.restore) {
          this.setCanvasData(forEach(canvasData, function (n, i) {
            canvasData[i] = n * ratio;
          }));
          this.setCropBoxData(forEach(cropBoxData, function (n, i) {
            cropBoxData[i] = n * ratio;
          }));
        }
      }
    },
    dblclick: function dblclick() {
      if (this.disabled || this.options.dragMode === DRAG_MODE_NONE) {
        return;
      }

      this.setDragMode(hasClass(this.dragBox, CLASS_CROP) ? DRAG_MODE_MOVE : DRAG_MODE_CROP);
    },
    wheel: function wheel(e) {
      var _this = this;

      var ratio = Number(this.options.wheelZoomRatio) || 0.1;
      var delta = 1;

      if (this.disabled) {
        return;
      }

      e.preventDefault();

      // Limit wheel speed to prevent zoom too fast (#21)
      if (this.wheeling) {
        return;
      }

      this.wheeling = true;

      setTimeout(function () {
        _this.wheeling = false;
      }, 50);

      if (e.deltaY) {
        delta = e.deltaY > 0 ? 1 : -1;
      } else if (e.wheelDelta) {
        delta = -e.wheelDelta / 120;
      } else if (e.detail) {
        delta = e.detail > 0 ? 1 : -1;
      }

      this.zoom(-delta * ratio, e);
    },
    cropStart: function cropStart(e) {
      if (this.disabled) {
        return;
      }

      var options = this.options,
          pointers = this.pointers;

      var action = void 0;

      if (e.changedTouches) {
        // Handle touch event
        forEach(e.changedTouches, function (touch) {
          pointers[touch.identifier] = getPointer(touch);
        });
      } else {
        // Handle mouse event and pointer event
        pointers[e.pointerId || 0] = getPointer(e);
      }

      console.log(this.disable)

      if (Object.keys(pointers).length > 1 && options.zoomable && options.zoomOnTouch) {
        action = ACTION_ZOOM;
      } else {
        action = getData(e.target, DATA_ACTION);
      }

      if (!REGEXP_ACTIONS.test(action)) {
        return;
      }

      if (dispatchEvent(this.element, EVENT_CROP_START, {
        originalEvent: e,
        action: action
      }) === false) {
        return;
      }

      e.preventDefault();

      this.action = action;
      this.cropping = false;

      if (action === ACTION_CROP) {
        this.cropping = true;
        addClass(this.dragBox, CLASS_MODAL);
      }
    },
    cropMove: function cropMove(e) {
      var action = this.action;
      console.log(action);

      if (this.disabled || !action) {
        return;
      }

      var pointers = this.pointers;


      e.preventDefault();

      if (dispatchEvent(this.element, EVENT_CROP_MOVE, {
        originalEvent: e,
        action: action
      }) === false) {
        return;
      }

      if (e.changedTouches) {
        forEach(e.changedTouches, function (touch) {
          assign(pointers[touch.identifier], getPointer(touch, true));
        });
      } else {
        assign(pointers[e.pointerId || 0], getPointer(e, true));
      }

      this.change(e);
    },
    cropEnd: function cropEnd(e) {
      if (this.disabled) {
        return;
      }

      var action = this.action,
          pointers = this.pointers;


      if (e.changedTouches) {
        forEach(e.changedTouches, function (touch) {
          delete pointers[touch.identifier];
        });
      } else {
        delete pointers[e.pointerId || 0];
      }

      if (!action) {
        return;
      }

      e.preventDefault();

      if (!Object.keys(pointers).length) {
        this.action = '';
      }

      if (this.cropping) {
        this.cropping = false;
        toggleClass(this.dragBox, CLASS_MODAL, this.cropped && this.options.modal);
      }

      dispatchEvent(this.element, EVENT_CROP_END, {
        originalEvent: e,
        action: action
      });
    }
  };

  var change = {
    change: function change(e) {
      var options = this.options,
          canvasData = this.canvasData,
          containerData = this.containerData,
          cropBoxData = this.cropBoxData,
          pointers = this.pointers;
      var action = this.action;
      var aspectRatio = options.aspectRatio;
      var left = cropBoxData.left,
          top = cropBoxData.top,
          width = cropBoxData.width,
          height = cropBoxData.height;

      var right = left + width;
      var bottom = top + height;
      var minLeft = 0;
      var minTop = 0;
      var maxWidth = containerData.width;
      var maxHeight = containerData.height;
      var renderable = true;
      var offset = void 0;

      // Locking aspect ratio in "free mode" by holding shift key
      if (!aspectRatio && e.shiftKey) {
        aspectRatio = width && height ? width / height : 1;
      }

      if (this.limited) {
        minLeft = cropBoxData.minLeft;
        minTop = cropBoxData.minTop;

        maxWidth = minLeft + Math.min(containerData.width, canvasData.width, canvasData.left + canvasData.width);
        maxHeight = minTop + Math.min(containerData.height, canvasData.height, canvasData.top + canvasData.height);
      }

      var pointer = pointers[Object.keys(pointers)[0]];
      var range = {
        x: pointer.endX - pointer.startX,
        y: pointer.endY - pointer.startY
      };
      var check = function check(side) {
        switch (side) {
          case ACTION_EAST:
            if (right + range.x > maxWidth) {
              range.x = maxWidth - right;
            }

            break;

          case ACTION_WEST:
            if (left + range.x < minLeft) {
              range.x = minLeft - left;
            }

            break;

          case ACTION_NORTH:
            if (top + range.y < minTop) {
              range.y = minTop - top;
            }

            break;

          case ACTION_SOUTH:
            if (bottom + range.y > maxHeight) {
              range.y = maxHeight - bottom;
            }

            break;

          default:
        }
      };

      switch (action) {
        // Move crop box
        case ACTION_ALL:
          left += range.x;
          top += range.y;
          break;

        // Resize crop box
        case ACTION_EAST:
          if (range.x >= 0 && (right >= maxWidth || aspectRatio && (top <= minTop || bottom >= maxHeight))) {
            renderable = false;
            break;
          }

          check(ACTION_EAST);
          width += range.x;

          if (aspectRatio) {
            height = width / aspectRatio;
            top -= range.x / aspectRatio / 2;
          }

          if (width < 0) {
            action = ACTION_WEST;
            width = 0;
          }

          break;

        case ACTION_NORTH:
          if (range.y <= 0 && (top <= minTop || aspectRatio && (left <= minLeft || right >= maxWidth))) {
            renderable = false;
            break;
          }

          check(ACTION_NORTH);
          height -= range.y;
          top += range.y;

          if (aspectRatio) {
            width = height * aspectRatio;
            left += range.y * aspectRatio / 2;
          }

          if (height < 0) {
            action = ACTION_SOUTH;
            height = 0;
          }

          break;

        case ACTION_WEST:
          if (range.x <= 0 && (left <= minLeft || aspectRatio && (top <= minTop || bottom >= maxHeight))) {
            renderable = false;
            break;
          }

          check(ACTION_WEST);
          width -= range.x;
          left += range.x;

          if (aspectRatio) {
            height = width / aspectRatio;
            top += range.x / aspectRatio / 2;
          }

          if (width < 0) {
            action = ACTION_EAST;
            width = 0;
          }

          break;

        case ACTION_SOUTH:
          if (range.y >= 0 && (bottom >= maxHeight || aspectRatio && (left <= minLeft || right >= maxWidth))) {
            renderable = false;
            break;
          }

          check(ACTION_SOUTH);
          height += range.y;

          if (aspectRatio) {
            width = height * aspectRatio;
            left -= range.y * aspectRatio / 2;
          }

          if (height < 0) {
            action = ACTION_NORTH;
            height = 0;
          }

          break;

        case ACTION_NORTH_EAST:
          if (aspectRatio) {
            if (range.y <= 0 && (top <= minTop || right >= maxWidth)) {
              renderable = false;
              break;
            }

            check(ACTION_NORTH);
            height -= range.y;
            top += range.y;
            width = height * aspectRatio;
          } else {
            check(ACTION_NORTH);
            check(ACTION_EAST);

            if (range.x >= 0) {
              if (right < maxWidth) {
                width += range.x;
              } else if (range.y <= 0 && top <= minTop) {
                renderable = false;
              }
            } else {
              width += range.x;
            }

            if (range.y <= 0) {
              if (top > minTop) {
                height -= range.y;
                top += range.y;
              }
            } else {
              height -= range.y;
              top += range.y;
            }
          }

          if (width < 0 && height < 0) {
            action = ACTION_SOUTH_WEST;
            height = 0;
            width = 0;
          } else if (width < 0) {
            action = ACTION_NORTH_WEST;
            width = 0;
          } else if (height < 0) {
            action = ACTION_SOUTH_EAST;
            height = 0;
          }

          break;

        case ACTION_NORTH_WEST:
          if (aspectRatio) {
            if (range.y <= 0 && (top <= minTop || left <= minLeft)) {
              renderable = false;
              break;
            }

            check(ACTION_NORTH);
            height -= range.y;
            top += range.y;
            width = height * aspectRatio;
            left += range.y * aspectRatio;
          } else {
            check(ACTION_NORTH);
            check(ACTION_WEST);

            if (range.x <= 0) {
              if (left > minLeft) {
                width -= range.x;
                left += range.x;
              } else if (range.y <= 0 && top <= minTop) {
                renderable = false;
              }
            } else {
              width -= range.x;
              left += range.x;
            }

            if (range.y <= 0) {
              if (top > minTop) {
                height -= range.y;
                top += range.y;
              }
            } else {
              height -= range.y;
              top += range.y;
            }
          }

          if (width < 0 && height < 0) {
            action = ACTION_SOUTH_EAST;
            height = 0;
            width = 0;
          } else if (width < 0) {
            action = ACTION_NORTH_EAST;
            width = 0;
          } else if (height < 0) {
            action = ACTION_SOUTH_WEST;
            height = 0;
          }

          break;

        case ACTION_SOUTH_WEST:
          if (aspectRatio) {
            if (range.x <= 0 && (left <= minLeft || bottom >= maxHeight)) {
              renderable = false;
              break;
            }

            check(ACTION_WEST);
            width -= range.x;
            left += range.x;
            height = width / aspectRatio;
          } else {
            check(ACTION_SOUTH);
            check(ACTION_WEST);

            if (range.x <= 0) {
              if (left > minLeft) {
                width -= range.x;
                left += range.x;
              } else if (range.y >= 0 && bottom >= maxHeight) {
                renderable = false;
              }
            } else {
              width -= range.x;
              left += range.x;
            }

            if (range.y >= 0) {
              if (bottom < maxHeight) {
                height += range.y;
              }
            } else {
              height += range.y;
            }
          }

          if (width < 0 && height < 0) {
            action = ACTION_NORTH_EAST;
            height = 0;
            width = 0;
          } else if (width < 0) {
            action = ACTION_SOUTH_EAST;
            width = 0;
          } else if (height < 0) {
            action = ACTION_NORTH_WEST;
            height = 0;
          }

          break;

        case ACTION_SOUTH_EAST:
          if (aspectRatio) {
            if (range.x >= 0 && (right >= maxWidth || bottom >= maxHeight)) {
              renderable = false;
              break;
            }

            check(ACTION_EAST);
            width += range.x;
            height = width / aspectRatio;
          } else {
            check(ACTION_SOUTH);
            check(ACTION_EAST);

            if (range.x >= 0) {
              if (right < maxWidth) {
                width += range.x;
              } else if (range.y >= 0 && bottom >= maxHeight) {
                renderable = false;
              }
            } else {
              width += range.x;
            }

            if (range.y >= 0) {
              if (bottom < maxHeight) {
                height += range.y;
              }
            } else {
              height += range.y;
            }
          }

          if (width < 0 && height < 0) {
            action = ACTION_NORTH_WEST;
            height = 0;
            width = 0;
          } else if (width < 0) {
            action = ACTION_SOUTH_WEST;
            width = 0;
          } else if (height < 0) {
            action = ACTION_NORTH_EAST;
            height = 0;
          }

          break;

        // Move canvas
        case ACTION_MOVE:
          this.move(range.x, range.y);
          renderable = false;
          break;

        // Zoom canvas
        case ACTION_ZOOM:
          this.zoom(getMaxZoomRatio(pointers), e);
          renderable = false;
          break;

        // Create crop box
        case ACTION_CROP:
          if (!range.x || !range.y) {
            renderable = false;
            break;
          }

          offset = getOffset(this.cropper);
          left = pointer.startX - offset.left;
          top = pointer.startY - offset.top;
          width = cropBoxData.minWidth;
          height = cropBoxData.minHeight;

          if (range.x > 0) {
            action = range.y > 0 ? ACTION_SOUTH_EAST : ACTION_NORTH_EAST;
          } else if (range.x < 0) {
            left -= width;
            action = range.y > 0 ? ACTION_SOUTH_WEST : ACTION_NORTH_WEST;
          }

          if (range.y < 0) {
            top -= height;
          }

          // Show the crop box if is hidden
          if (!this.cropped) {
            removeClass(this.cropBox, CLASS_HIDDEN);
            this.cropped = true;

            if (this.limited) {
              this.limitCropBox(true, true);
            }
          }

          break;

        default:
      }

      if (renderable) {
        cropBoxData.width = width;
        cropBoxData.height = height;
        cropBoxData.left = left;
        cropBoxData.top = top;
        this.action = action;
        this.renderCropBox();
      }

      // Override
      forEach(pointers, function (p) {
        p.startX = p.endX;
        p.startY = p.endY;
      });
    }
  };

  var methods = {
    // Show the crop box manually
    crop: function crop() {
      if (this.ready && !this.cropped && !this.disabled) {
        this.cropped = true;
        this.limitCropBox(true, true);

        if (this.options.modal) {
          addClass(this.dragBox, CLASS_MODAL);
        }

        removeClass(this.cropBox, CLASS_HIDDEN);
        this.setCropBoxData(this.initialCropBoxData);
      }

      return this;
    },


    // Reset the image and crop box to their initial states
    reset: function reset() {
      if (this.ready && !this.disabled) {
        this.imageData = assign({}, this.initialImageData);
        this.canvasData = assign({}, this.initialCanvasData);
        this.cropBoxData = assign({}, this.initialCropBoxData);
        this.renderCanvas();

        if (this.cropped) {
          this.renderCropBox();
        }
      }

      return this;
    },


    // Clear the crop box
    clear: function clear() {
      if (this.cropped && !this.disabled) {
        assign(this.cropBoxData, {
          left: 0,
          top: 0,
          width: 0,
          height: 0
        });

        this.cropped = false;
        this.renderCropBox();
        this.limitCanvas(true, true);

        // Render canvas after crop box rendered
        this.renderCanvas();
        removeClass(this.dragBox, CLASS_MODAL);
        addClass(this.cropBox, CLASS_HIDDEN);
      }

      return this;
    },


    /**
     * Replace the image's src and rebuild the cropper
     * @param {string} url - The new URL.
     * @param {boolean} [hasSameSize] - Indicate if the new image has the same size as the old one.
     * @returns {Cropper} this
     */
    replace: function replace(url) {
      var hasSameSize = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;

      if (!this.disabled && url) {
        if (this.isImg) {
          this.element.src = url;
        }

        if (hasSameSize) {
          this.url = url;
          this.image.src = url;

          if (this.ready) {
            this.viewBoxImage.src = url;

            forEach(this.previews, function (element) {
              element.getElementsByTagName('img')[0].src = url;
            });
          }
        } else {
          if (this.isImg) {
            this.replaced = true;
          }

          this.options.data = null;
          this.uncreate();
          this.load(url);
        }
      }

      return this;
    },


    // Enable (unfreeze) the cropper
    enable: function enable() {
      if (this.ready && this.disabled) {
        this.disabled = false;
        removeClass(this.cropper, CLASS_DISABLED);
      }

      return this;
    },


    // Disable (freeze) the cropper
    disable: function disable() {
      if (this.ready && !this.disabled) {
        this.disabled = true;
        addClass(this.cropper, CLASS_DISABLED);
      }

      return this;
    },


    /**
     * Destroy the cropper and remove the instance from the image
     * @returns {Cropper} this
     */
    destroy: function destroy() {
      var element = this.element;


      if (!getData(element, NAMESPACE)) {
        return this;
      }

      if (this.isImg && this.replaced) {
        element.src = this.originalUrl;
      }

      this.uncreate();
      removeData(element, NAMESPACE);

      return this;
    },


    /**
     * Move the canvas with relative offsets
     * @param {number} offsetX - The relative offset distance on the x-axis.
     * @param {number} [offsetY=offsetX] - The relative offset distance on the y-axis.
     * @returns {Cropper} this
     */
    move: function move(offsetX) {
      var offsetY = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : offsetX;
      var _canvasData = this.canvasData,
          left = _canvasData.left,
          top = _canvasData.top;


      return this.moveTo(isUndefined(offsetX) ? offsetX : left + Number(offsetX), isUndefined(offsetY) ? offsetY : top + Number(offsetY));
    },


    /**
     * Move the canvas to an absolute point
     * @param {number} x - The x-axis coordinate.
     * @param {number} [y=x] - The y-axis coordinate.
     * @returns {Cropper} this
     */
    moveTo: function moveTo(x) {
      var y = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : x;
      var canvasData = this.canvasData;

      var changed = false;

      x = Number(x);
      y = Number(y);

      if (this.ready && !this.disabled && this.options.movable) {
        if (isNumber(x)) {
          canvasData.left = x;
          changed = true;
        }

        if (isNumber(y)) {
          canvasData.top = y;
          changed = true;
        }

        if (changed) {
          this.renderCanvas(true);
        }
      }

      return this;
    },


    /**
     * Zoom the canvas with a relative ratio
     * @param {number} ratio - The target ratio.
     * @param {Event} _originalEvent - The original event if any.
     * @returns {Cropper} this
     */
    zoom: function zoom(ratio, _originalEvent) {
      var canvasData = this.canvasData;


      ratio = Number(ratio);

      if (ratio < 0) {
        ratio = 1 / (1 - ratio);
      } else {
        ratio = 1 + ratio;
      }

      return this.zoomTo(canvasData.width * ratio / canvasData.naturalWidth, null, _originalEvent);
    },


    /**
     * Zoom the canvas to an absolute ratio
     * @param {number} ratio - The target ratio.
     * @param {Object} pivot - The zoom pivot point coordinate.
     * @param {Event} _originalEvent - The original event if any.
     * @returns {Cropper} this
     */
    zoomTo: function zoomTo(ratio, pivot, _originalEvent) {
      var options = this.options,
          canvasData = this.canvasData;
      var width = canvasData.width,
          height = canvasData.height,
          naturalWidth = canvasData.naturalWidth,
          naturalHeight = canvasData.naturalHeight;


      ratio = Number(ratio);

      if (ratio >= 0 && this.ready && !this.disabled && options.zoomable) {
        var newWidth = naturalWidth * ratio;
        var newHeight = naturalHeight * ratio;

        if (dispatchEvent(this.element, EVENT_ZOOM, {
          originalEvent: _originalEvent,
          oldRatio: width / naturalWidth,
          ratio: newWidth / naturalWidth
        }) === false) {
          return this;
        }

        if (_originalEvent) {
          var pointers = this.pointers;

          var offset = getOffset(this.cropper);
          var center = pointers && Object.keys(pointers).length ? getPointersCenter(pointers) : {
            pageX: _originalEvent.pageX || _originalEvent.originalEvent.pageX,
            pageY: _originalEvent.pageY || _originalEvent.originalEvent.pageY
          };

          // Zoom from the triggering point of the event
          canvasData.left -= (newWidth - width) * ((center.pageX - offset.left - canvasData.left) / width);
          canvasData.top -= (newHeight - height) * ((center.pageY - offset.top - canvasData.top) / height);
        } else if (isPlainObject(pivot) && isNumber(pivot.x) && isNumber(pivot.y)) {
          canvasData.left -= (newWidth - width) * ((pivot.x - canvasData.left) / width);
          canvasData.top -= (newHeight - height) * ((pivot.y - canvasData.top) / height);
        } else {
          // Zoom from the center of the canvas
          canvasData.left -= (newWidth - width) / 2;
          canvasData.top -= (newHeight - height) / 2;
        }

        canvasData.width = newWidth;
        canvasData.height = newHeight;
        this.renderCanvas(true);
      }

      return this;
    },


    /**
     * Rotate the canvas with a relative degree
     * @param {number} degree - The rotate degree.
     * @returns {Cropper} this
     */
    rotate: function rotate(degree) {
      return this.rotateTo((this.imageData.rotate || 0) + Number(degree));
    },


    /**
     * Rotate the canvas to an absolute degree
     * @param {number} degree - The rotate degree.
     * @returns {Cropper} this
     */
    rotateTo: function rotateTo(degree) {
      degree = Number(degree);

      if (isNumber(degree) && this.ready && !this.disabled && this.options.rotatable) {
        this.imageData.rotate = degree % 360;
        this.renderCanvas(true, true);
      }

      return this;
    },


    /**
     * Scale the image on the x-axis.
     * @param {number} scaleX - The scale ratio on the x-axis.
     * @returns {Cropper} this
     */
    scaleX: function scaleX(_scaleX) {
      var scaleY = this.imageData.scaleY;


      return this.scale(_scaleX, isNumber(scaleY) ? scaleY : 1);
    },


    /**
     * Scale the image on the y-axis.
     * @param {number} scaleY - The scale ratio on the y-axis.
     * @returns {Cropper} this
     */
    scaleY: function scaleY(_scaleY) {
      var scaleX = this.imageData.scaleX;


      return this.scale(isNumber(scaleX) ? scaleX : 1, _scaleY);
    },


    /**
     * Scale the image
     * @param {number} scaleX - The scale ratio on the x-axis.
     * @param {number} [scaleY=scaleX] - The scale ratio on the y-axis.
     * @returns {Cropper} this
     */
    scale: function scale(scaleX) {
      var scaleY = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : scaleX;
      var imageData = this.imageData;

      var transformed = false;

      scaleX = Number(scaleX);
      scaleY = Number(scaleY);

      if (this.ready && !this.disabled && this.options.scalable) {
        if (isNumber(scaleX)) {
          imageData.scaleX = scaleX;
          transformed = true;
        }

        if (isNumber(scaleY)) {
          imageData.scaleY = scaleY;
          transformed = true;
        }

        if (transformed) {
          this.renderCanvas(true, true);
        }
      }

      return this;
    },


    /**
     * Get the cropped area position and size data (base on the original image)
     * @param {boolean} [rounded=false] - Indicate if round the data values or not.
     * @returns {Object} The result cropped data.
     */
    getData: function getData$$1() {
      var rounded = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;
      var options = this.options,
          imageData = this.imageData,
          canvasData = this.canvasData,
          cropBoxData = this.cropBoxData;

      var data = void 0;

      if (this.ready && this.cropped) {
        data = {
          x: cropBoxData.left - canvasData.left,
          y: cropBoxData.top - canvasData.top,
          width: cropBoxData.width,
          height: cropBoxData.height
        };

        var ratio = imageData.width / imageData.naturalWidth;

        forEach(data, function (n, i) {
          n /= ratio;
          data[i] = rounded ? Math.round(n) : n;
        });
      } else {
        data = {
          x: 0,
          y: 0,
          width: 0,
          height: 0
        };
      }

      if (options.rotatable) {
        data.rotate = imageData.rotate || 0;
      }

      if (options.scalable) {
        data.scaleX = imageData.scaleX || 1;
        data.scaleY = imageData.scaleY || 1;
      }

      return data;
    },


    /**
     * Set the cropped area position and size with new data
     * @param {Object} data - The new data.
     * @returns {Cropper} this
     */
    setData: function setData$$1(data) {
      var options = this.options,
          imageData = this.imageData,
          canvasData = this.canvasData;

      var cropBoxData = {};

      if (this.ready && !this.disabled && isPlainObject(data)) {
        var transformed = false;

        if (options.rotatable) {
          if (isNumber(data.rotate) && data.rotate !== imageData.rotate) {
            imageData.rotate = data.rotate;
            transformed = true;
          }
        }

        if (options.scalable) {
          if (isNumber(data.scaleX) && data.scaleX !== imageData.scaleX) {
            imageData.scaleX = data.scaleX;
            transformed = true;
          }

          if (isNumber(data.scaleY) && data.scaleY !== imageData.scaleY) {
            imageData.scaleY = data.scaleY;
            transformed = true;
          }
        }

        if (transformed) {
          this.renderCanvas(true, true);
        }

        var ratio = imageData.width / imageData.naturalWidth;

        if (isNumber(data.x)) {
          cropBoxData.left = data.x * ratio + canvasData.left;
        }

        if (isNumber(data.y)) {
          cropBoxData.top = data.y * ratio + canvasData.top;
        }

        if (isNumber(data.width)) {
          cropBoxData.width = data.width * ratio;
        }

        if (isNumber(data.height)) {
          cropBoxData.height = data.height * ratio;
        }

        this.setCropBoxData(cropBoxData);
      }

      return this;
    },


    /**
     * Get the container size data.
     * @returns {Object} The result container data.
     */
    getContainerData: function getContainerData() {
      return this.ready ? assign({}, this.containerData) : {};
    },


    /**
     * Get the image position and size data.
     * @returns {Object} The result image data.
     */
    getImageData: function getImageData() {
      return this.sized ? assign({}, this.imageData) : {};
    },


    /**
     * Get the canvas position and size data.
     * @returns {Object} The result canvas data.
     */
    getCanvasData: function getCanvasData() {
      var canvasData = this.canvasData;

      var data = {};

      if (this.ready) {
        forEach(['left', 'top', 'width', 'height', 'naturalWidth', 'naturalHeight'], function (n) {
          data[n] = canvasData[n];
        });
      }

      return data;
    },


    /**
     * Set the canvas position and size with new data.
     * @param {Object} data - The new canvas data.
     * @returns {Cropper} this
     */
    setCanvasData: function setCanvasData(data) {
      var canvasData = this.canvasData;
      var aspectRatio = canvasData.aspectRatio;


      if (this.ready && !this.disabled && isPlainObject(data)) {
        if (isNumber(data.left)) {
          canvasData.left = data.left;
        }

        if (isNumber(data.top)) {
          canvasData.top = data.top;
        }

        if (isNumber(data.width)) {
          canvasData.width = data.width;
          canvasData.height = data.width / aspectRatio;
        } else if (isNumber(data.height)) {
          canvasData.height = data.height;
          canvasData.width = data.height * aspectRatio;
        }

        this.renderCanvas(true);
      }

      return this;
    },


    /**
     * Get the crop box position and size data.
     * @returns {Object} The result crop box data.
     */
    getCropBoxData: function getCropBoxData() {
      var cropBoxData = this.cropBoxData;

      var data = void 0;

      if (this.ready && this.cropped) {
        data = {
          left: cropBoxData.left,
          top: cropBoxData.top,
          width: cropBoxData.width,
          height: cropBoxData.height
        };
      }

      return data || {};
    },


    /**
     * Set the crop box position and size with new data.
     * @param {Object} data - The new crop box data.
     * @returns {Cropper} this
     */
    setCropBoxData: function setCropBoxData(data) {
      var cropBoxData = this.cropBoxData;
      var aspectRatio = this.options.aspectRatio;

      var widthChanged = void 0;
      var heightChanged = void 0;

      if (this.ready && this.cropped && !this.disabled && isPlainObject(data)) {
        if (isNumber(data.left)) {
          cropBoxData.left = data.left;
        }

        if (isNumber(data.top)) {
          cropBoxData.top = data.top;
        }

        if (isNumber(data.width) && data.width !== cropBoxData.width) {
          widthChanged = true;
          cropBoxData.width = data.width;
        }

        if (isNumber(data.height) && data.height !== cropBoxData.height) {
          heightChanged = true;
          cropBoxData.height = data.height;
        }

        if (aspectRatio) {
          if (widthChanged) {
            cropBoxData.height = cropBoxData.width / aspectRatio;
          } else if (heightChanged) {
            cropBoxData.width = cropBoxData.height * aspectRatio;
          }
        }

        this.renderCropBox();
      }

      return this;
    },


    /**
     * Get a canvas drawn the cropped image.
     * @param {Object} [options={}] - The config options.
     * @returns {HTMLCanvasElement} - The result canvas.
     */
    getCroppedCanvas: function getCroppedCanvas() {
      var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

      if (!this.ready || !window.HTMLCanvasElement) {
        return null;
      }

      var canvasData = this.canvasData;

      var source = getSourceCanvas(this.image, this.imageData, canvasData, options);

      // Returns the source canvas if it is not cropped.
      if (!this.cropped) {
        return source;
      }

      var _getData = this.getData(),
          initialX = _getData.x,
          initialY = _getData.y,
          initialWidth = _getData.width,
          initialHeight = _getData.height;

      var ratio = source.width / Math.floor(canvasData.naturalWidth);

      if (ratio !== 1) {
        initialX *= ratio;
        initialY *= ratio;
        initialWidth *= ratio;
        initialHeight *= ratio;
      }

      var aspectRatio = initialWidth / initialHeight;
      var maxSizes = getAdjustedSizes({
        aspectRatio: aspectRatio,
        width: options.maxWidth || Infinity,
        height: options.maxHeight || Infinity
      });
      var minSizes = getAdjustedSizes({
        aspectRatio: aspectRatio,
        width: options.minWidth || 0,
        height: options.minHeight || 0
      }, 'cover');

      var _getAdjustedSizes = getAdjustedSizes({
        aspectRatio: aspectRatio,
        width: options.width || (ratio !== 1 ? source.width : initialWidth),
        height: options.height || (ratio !== 1 ? source.height : initialHeight)
      }),
          width = _getAdjustedSizes.width,
          height = _getAdjustedSizes.height;

      width = Math.min(maxSizes.width, Math.max(minSizes.width, width));
      height = Math.min(maxSizes.height, Math.max(minSizes.height, height));

      var canvas = document.createElement('canvas');
      var context = canvas.getContext('2d');

      canvas.width = normalizeDecimalNumber(width);
      canvas.height = normalizeDecimalNumber(height);

      context.fillStyle = options.fillColor || 'transparent';
      context.fillRect(0, 0, width, height);

      var _options$imageSmoothi = options.imageSmoothingEnabled,
          imageSmoothingEnabled = _options$imageSmoothi === undefined ? true : _options$imageSmoothi,
          imageSmoothingQuality = options.imageSmoothingQuality;


      context.imageSmoothingEnabled = imageSmoothingEnabled;

      if (imageSmoothingQuality) {
        context.imageSmoothingQuality = imageSmoothingQuality;
      }

      // https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D.drawImage
      var sourceWidth = source.width;
      var sourceHeight = source.height;

      // Source canvas parameters
      var srcX = initialX;
      var srcY = initialY;
      var srcWidth = void 0;
      var srcHeight = void 0;

      // Destination canvas parameters
      var dstX = void 0;
      var dstY = void 0;
      var dstWidth = void 0;
      var dstHeight = void 0;

      if (srcX <= -initialWidth || srcX > sourceWidth) {
        srcX = 0;
        srcWidth = 0;
        dstX = 0;
        dstWidth = 0;
      } else if (srcX <= 0) {
        dstX = -srcX;
        srcX = 0;
        srcWidth = Math.min(sourceWidth, initialWidth + srcX);
        dstWidth = srcWidth;
      } else if (srcX <= sourceWidth) {
        dstX = 0;
        srcWidth = Math.min(initialWidth, sourceWidth - srcX);
        dstWidth = srcWidth;
      }

      if (srcWidth <= 0 || srcY <= -initialHeight || srcY > sourceHeight) {
        srcY = 0;
        srcHeight = 0;
        dstY = 0;
        dstHeight = 0;
      } else if (srcY <= 0) {
        dstY = -srcY;
        srcY = 0;
        srcHeight = Math.min(sourceHeight, initialHeight + srcY);
        dstHeight = srcHeight;
      } else if (srcY <= sourceHeight) {
        dstY = 0;
        srcHeight = Math.min(initialHeight, sourceHeight - srcY);
        dstHeight = srcHeight;
      }

      var params = [srcX, srcY, srcWidth, srcHeight];

      // Avoid "IndexSizeError"
      if (dstWidth > 0 && dstHeight > 0) {
        var scale = width / initialWidth;

        params.push(dstX * scale, dstY * scale, dstWidth * scale, dstHeight * scale);
      }

      // All the numerical parameters should be integer for `drawImage`
      // https://github.com/fengyuanchen/cropper/issues/476
      context.drawImage.apply(context, [source].concat(toConsumableArray(params.map(function (param) {
        return Math.floor(normalizeDecimalNumber(param));
      }))));

      return canvas;
    },


    /**
     * Change the aspect ratio of the crop box.
     * @param {number} aspectRatio - The new aspect ratio.
     * @returns {Cropper} this
     */
    setAspectRatio: function setAspectRatio(aspectRatio) {
      var options = this.options;


      if (!this.disabled && !isUndefined(aspectRatio)) {
        // 0 -> NaN
        options.aspectRatio = Math.max(0, aspectRatio) || NaN;

        if (this.ready) {
          this.initCropBox();

          if (this.cropped) {
            this.renderCropBox();
          }
        }
      }

      return this;
    },


    /**
     * Change the drag mode.
     * @param {string} mode - The new drag mode.
     * @returns {Cropper} this
     */
    setDragMode: function setDragMode(mode) {
      var options = this.options,
          dragBox = this.dragBox,
          face = this.face;


      if (this.ready && !this.disabled) {
        var croppable = mode === DRAG_MODE_CROP;
        var movable = options.movable && mode === DRAG_MODE_MOVE;

        mode = croppable || movable ? mode : DRAG_MODE_NONE;

        options.dragMode = mode;
        setData(dragBox, DATA_ACTION, mode);
        toggleClass(dragBox, CLASS_CROP, croppable);
        toggleClass(dragBox, CLASS_MOVE, movable);

        if (!options.cropBoxMovable) {
          // Sync drag mode to crop box when it is not movable
          setData(face, DATA_ACTION, mode);
          toggleClass(face, CLASS_CROP, croppable);
          toggleClass(face, CLASS_MOVE, movable);
        }
      }

      return this;
    }
  };

  var AnotherCropper = WINDOW.Cropper;

  var Cropper = function () {
    /**
     * Create a new Cropper.
     * @param {Element} element - The target element for cropping.
     * @param {Object} [options={}] - The configuration options.
     */
    function Cropper(element) {
      var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
      classCallCheck(this, Cropper);

      if (!element || !REGEXP_TAG_NAME.test(element.tagName)) {
        throw new Error('The first argument is required and must be an <img> or <canvas> element.');
      }

      this.element = element;
      this.options = assign({}, DEFAULTS, isPlainObject(options) && options);
      this.cropped = false;
      this.disabled = false;
      this.pointers = {};
      this.ready = false;
      this.reloading = false;
      this.replaced = false;
      this.sized = false;
      this.sizing = false;
      this.init();
    }

    createClass(Cropper, [{
      key: 'init',
      value: function init() {
        var element = this.element;

        var tagName = element.tagName.toLowerCase();
        var url = void 0;

        if (getData(element, NAMESPACE)) {
          return;
        }

        setData(element, NAMESPACE, this);

        if (tagName === 'img') {
          this.isImg = true;

          // e.g.: "img/picture.jpg"
          url = element.getAttribute('src') || '';
          this.originalUrl = url;

          // Stop when it's a blank image
          if (!url) {
            return;
          }

          // e.g.: "http://example.com/img/picture.jpg"
          url = element.src;
        } else if (tagName === 'canvas' && window.HTMLCanvasElement) {
          url = element.toDataURL();
        }

        this.load(url);
      }
    }, {
      key: 'load',
      value: function load(url) {
        var _this = this;

        if (!url) {
          return;
        }

        this.url = url;
        this.imageData = {};

        var element = this.element,
            options = this.options;


        if (!options.checkOrientation || !window.ArrayBuffer) {
          this.clone();
          return;
        }

        // XMLHttpRequest disallows to open a Data URL in some browsers like IE11 and Safari
        if (REGEXP_DATA_URL.test(url)) {
          if (REGEXP_DATA_URL_JPEG.test(url)) {
            this.read(dataURLToArrayBuffer(url));
          } else {
            this.clone();
          }

          return;
        }

        var xhr = new XMLHttpRequest();

        this.reloading = true;
        this.xhr = xhr;

        var done = function done() {
          _this.reloading = false;
          _this.xhr = null;
        };

        xhr.ontimeout = done;
        xhr.onabort = done;
        xhr.onerror = function () {
          done();
          _this.clone();
        };

        xhr.onload = function () {
          done();
          _this.read(xhr.response);
        };

        // Bust cache when there is a "crossOrigin" property
        if (options.checkCrossOrigin && isCrossOriginURL(url) && element.crossOrigin) {
          url = addTimestamp(url);
        }

        xhr.open('get', url);
        xhr.responseType = 'arraybuffer';
        xhr.withCredentials = element.crossOrigin === 'use-credentials';
        xhr.send();
      }
    }, {
      key: 'read',
      value: function read(arrayBuffer) {
        var options = this.options,
            imageData = this.imageData;

        var orientation = getOrientation(arrayBuffer);
        var rotate = 0;
        var scaleX = 1;
        var scaleY = 1;

        if (orientation > 1) {
          this.url = arrayBufferToDataURL(arrayBuffer, 'image/jpeg');

          var _parseOrientation = parseOrientation(orientation);

          rotate = _parseOrientation.rotate;
          scaleX = _parseOrientation.scaleX;
          scaleY = _parseOrientation.scaleY;
        }

        if (options.rotatable) {
          imageData.rotate = rotate;
        }

        if (options.scalable) {
          imageData.scaleX = scaleX;
          imageData.scaleY = scaleY;
        }

        this.clone();
      }
    }, {
      key: 'clone',
      value: function clone() {
        var element = this.element,
            url = this.url;

        var crossOrigin = void 0;
        var crossOriginUrl = void 0;

        if (this.options.checkCrossOrigin && isCrossOriginURL(url)) {
          crossOrigin = element.crossOrigin;


          if (crossOrigin) {
            crossOriginUrl = url;
          } else {
            crossOrigin = 'anonymous';

            // Bust cache when there is not a "crossOrigin" property
            crossOriginUrl = addTimestamp(url);
          }
        }

        this.crossOrigin = crossOrigin;
        this.crossOriginUrl = crossOriginUrl;

        var image = document.createElement('img');

        if (crossOrigin) {
          image.crossOrigin = crossOrigin;
        }

        image.src = crossOriginUrl || url;

        var start = this.start.bind(this);
        var stop = this.stop.bind(this);

        this.image = image;
        this.onStart = start;
        this.onStop = stop;

        if (this.isImg) {
          if (element.complete) {
            // start asynchronously to keep `this.cropper` is accessible in `ready` event handler.
            this.timeout = setTimeout(start, 0);
          } else {
            addListener(element, EVENT_LOAD, start, {
              once: true
            });
          }
        } else {
          image.onload = start;
          image.onerror = stop;
          addClass(image, CLASS_HIDE);
          element.parentNode.insertBefore(image, element.nextSibling);
        }
      }
    }, {
      key: 'start',
      value: function start(event) {
        var _this2 = this;

        var image = this.isImg ? this.element : this.image;

        if (event) {
          image.onload = null;
          image.onerror = null;
        }

        this.sizing = true;

        var IS_SAFARI = WINDOW.navigator && /(Macintosh|iPhone|iPod|iPad).*AppleWebKit/i.test(WINDOW.navigator.userAgent);
        var done = function done(naturalWidth, naturalHeight) {
          assign(_this2.imageData, {
            naturalWidth: naturalWidth,
            naturalHeight: naturalHeight,
            aspectRatio: naturalWidth / naturalHeight
          });
          _this2.sizing = false;
          _this2.sized = true;
          _this2.build();
        };

        // Modern browsers (except Safari)
        if (image.naturalWidth && !IS_SAFARI) {
          done(image.naturalWidth, image.naturalHeight);
          return;
        }

        var sizingImage = document.createElement('img');
        var body = document.body || document.documentElement;

        this.sizingImage = sizingImage;

        sizingImage.onload = function () {
          done(sizingImage.width, sizingImage.height);

          if (!IS_SAFARI) {
            body.removeChild(sizingImage);
          }
        };

        sizingImage.src = image.src;

        // iOS Safari will convert the image automatically
        // with its orientation once append it into DOM (#279)
        if (!IS_SAFARI) {
          sizingImage.style.cssText = 'left:0;' + 'max-height:none!important;' + 'max-width:none!important;' + 'min-height:0!important;' + 'min-width:0!important;' + 'opacity:0;' + 'position:absolute;' + 'top:0;' + 'z-index:-1;';
          body.appendChild(sizingImage);
        }
      }
    }, {
      key: 'stop',
      value: function stop() {
        var image = this.image;


        image.onload = null;
        image.onerror = null;
        image.parentNode.removeChild(image);
        this.image = null;
      }
    }, {
      key: 'build',
      value: function build() {
        if (!this.sized || this.ready) {
          return;
        }

        var element = this.element,
            options = this.options,
            image = this.image;

        // Create cropper elements

        var container = element.parentNode;
        var template = document.createElement('div');

        template.innerHTML = TEMPLATE;

        var cropper = template.querySelector('.' + NAMESPACE + '-container');
        var canvas = cropper.querySelector('.' + NAMESPACE + '-canvas');
        var dragBox = cropper.querySelector('.' + NAMESPACE + '-drag-box');
        var cropBox = cropper.querySelector('.' + NAMESPACE + '-crop-box');
        var face = cropBox.querySelector('.' + NAMESPACE + '-face');

        this.container = container;
        this.cropper = cropper;
        this.canvas = canvas;
        this.dragBox = dragBox;
        this.cropBox = cropBox;
        this.viewBox = cropper.querySelector('.' + NAMESPACE + '-view-box');
        this.face = face;

        canvas.appendChild(image);

        // Hide the original image
        addClass(element, CLASS_HIDDEN);

        // Inserts the cropper after to the current image
        container.insertBefore(cropper, element.nextSibling);

        // Show the image if is hidden
        if (!this.isImg) {
          removeClass(image, CLASS_HIDE);
        }

        this.initPreview();
        this.bind();

        options.aspectRatio = Math.max(0, options.aspectRatio) || NaN;
        options.viewMode = Math.max(0, Math.min(3, Math.round(options.viewMode))) || 0;

        addClass(cropBox, CLASS_HIDDEN);

        if (!options.guides) {
          addClass(cropBox.getElementsByClassName(NAMESPACE + '-dashed'), CLASS_HIDDEN);
        }

        if (!options.center) {
          addClass(cropBox.getElementsByClassName(NAMESPACE + '-center'), CLASS_HIDDEN);
        }

        if (options.background) {
          addClass(cropper, NAMESPACE + '-bg');
        }

        if (!options.highlight) {
          addClass(face, CLASS_INVISIBLE);
        }

        if (options.cropBoxMovable) {
          addClass(face, CLASS_MOVE);
          setData(face, DATA_ACTION, ACTION_ALL);
        }

        if (!options.cropBoxResizable) {
          addClass(cropBox.getElementsByClassName(NAMESPACE + '-line'), CLASS_HIDDEN);
          addClass(cropBox.getElementsByClassName(NAMESPACE + '-point'), CLASS_HIDDEN);
        }

        this.render();
        this.ready = true;
        this.setDragMode(options.dragMode);

        if (options.autoCrop) {
          this.crop();
        }

        this.setData(options.data);

        if (isFunction(options.ready)) {
          addListener(element, EVENT_READY, options.ready, {
            once: true
          });
        }

        dispatchEvent(element, EVENT_READY);
      }
    }, {
      key: 'unbuild',
      value: function unbuild() {
        if (!this.ready) {
          return;
        }

        this.ready = false;
        this.unbind();
        this.resetPreview();
        this.cropper.parentNode.removeChild(this.cropper);
        removeClass(this.element, CLASS_HIDDEN);
      }
    }, {
      key: 'uncreate',
      value: function uncreate() {
        var element = this.element;


        if (this.ready) {
          this.unbuild();
          this.ready = false;
          this.cropped = false;
        } else if (this.sizing) {
          this.sizingImage.onload = null;
          this.sizing = false;
          this.sized = false;
        } else if (this.reloading) {
          this.xhr.abort();
        } else if (this.isImg) {
          if (element.complete) {
            clearTimeout(this.timeout);
          } else {
            removeListener(element, EVENT_LOAD, this.onStart);
          }
        } else if (this.image) {
          this.stop();
        }
      }

      /**
       * Get the no conflict cropper class.
       * @returns {Cropper} The cropper class.
       */

    }], [{
      key: 'noConflict',
      value: function noConflict() {
        window.Cropper = AnotherCropper;
        return Cropper;
      }

      /**
       * Change the default options.
       * @param {Object} options - The new default options.
       */

    }, {
      key: 'setDefaults',
      value: function setDefaults(options) {
        assign(DEFAULTS, isPlainObject(options) && options);
      }
    }]);
    return Cropper;
  }();

  assign(Cropper.prototype, render, preview, events, handlers, change, methods);

  if ($.fn) {
    var AnotherCropper$1 = $.fn.cropper;
    var NAMESPACE$1 = 'cropper';

    $.fn.cropper = function jQueryCropper(option) {
      for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
        args[_key - 1] = arguments[_key];
      }

      var result = void 0;

      this.each(function (i, element) {
        var $element = $(element);
        var isDestroy = option === 'destroy';
        var cropper = $element.data(NAMESPACE$1);

        if (!cropper) {
          if (isDestroy) {
            return;
          }

          var options = $.extend({}, $element.data(), $.isPlainObject(option) && option);

          cropper = new Cropper(element, options);
          $element.data(NAMESPACE$1, cropper);
        }

        if (typeof option === 'string') {
          var fn = cropper[option];

          if ($.isFunction(fn)) {
            result = fn.apply(cropper, args);

            if (result === cropper) {
              result = undefined;
            }

            if (isDestroy) {
              $element.removeData(NAMESPACE$1);
            }
          }
        }
      });

      return result !== undefined ? result : this;
    };

    $.fn.cropper.Constructor = Cropper;
    $.fn.cropper.setDefaults = Cropper.setDefaults;
    $.fn.cropper.noConflict = function noConflict() {
      $.fn.cropper = AnotherCropper$1;
      return this;
    };


    $.fn.cropper.setDefaults({
      checkImageOrigin: false
    });
  }

})));

;/*!src/static/lib/layer.js*/
/*! layer-v2.4.0 Web弹层组件 LGPL License  http://layer.layui.com/  By 贤心 */
 ;!function(e,t){"use strict";var i,n,a={getPath:function(){var e=document.scripts,t=e[e.length-1],i=t.src;if(!t.getAttribute("merge"))return i.substring(0,i.lastIndexOf("/")+1)}(),enter:function(e){13===e.keyCode&&e.preventDefault()},config:{},end:{},btn:["&#x786E;&#x5B9A;","&#x53D6;&#x6D88;"],type:["dialog","page","iframe","loading","tips"]},o={v:"2.4",ie6:!!e.ActiveXObject&&!e.XMLHttpRequest,index:e.layer&&e.layer.v?1e5:0,path:a.getPath,config:function(e,t){var n=0;return e=e||{},o.cache=a.config=i.extend(a.config,e),o.path=a.config.path||o.path,"string"==typeof e.extend&&(e.extend=[e.extend]),o.use("skin/default/layer.css",e.extend&&e.extend.length>0?function r(){var i=e.extend;o.use(i[i[n]?n:n-1],n<i.length?function(){return++n,r}():t)}():t),this},use:function(e,t,n){var a=i("head")[0],e=e.replace(/\s/g,""),r=/\.css$/.test(e),l=document.createElement(r?"link":"script"),s="layui_layer_"+e.replace(/\.|\//g,"");if(o.path)return r&&(l.rel="stylesheet"),l[r?"href":"src"]=/^http:\/\//.test(e)?e:o.path+e,l.id=s,i("#"+s)[0]||a.appendChild(l),function c(){(r?1989===parseInt(i("#"+s).css("width")):o[n||s])?function(){t&&t();try{r||a.removeChild(l)}catch(e){}}():setTimeout(c,100)}(),this},ready:function(e,t){var n="function"==typeof e;return n&&(t=e),o.config(i.extend(a.config,function(){return n?{}:{path:e}}()),t),this},alert:function(e,t,n){var a="function"==typeof t;return a&&(n=t),o.open(i.extend({content:e,yes:n},a?{}:t))},confirm:function(e,t,n,r){var l="function"==typeof t;return l&&(r=n,n=t),o.open(i.extend({content:e,btn:a.btn,yes:n,btn2:r},l?{}:t))},msg:function(e,n,r){var s="function"==typeof n,c=a.config.skin,f=(c?c+" "+c+"-msg":"")||"layui-layer-msg",u=l.anim.length-1;return s&&(r=n),o.open(i.extend({content:e,time:3e3,shade:!1,skin:f,title:!1,closeBtn:!1,btn:!1,end:r},s&&!a.config.skin?{skin:f+" layui-layer-hui",shift:u}:function(){return n=n||{},(n.icon===-1||n.icon===t&&!a.config.skin)&&(n.skin=f+" "+(n.skin||"layui-layer-hui")),n}()))},load:function(e,t){return o.open(i.extend({type:3,icon:e||0,shade:.01},t))},tips:function(e,t,n){return o.open(i.extend({type:4,content:[e,t],closeBtn:!1,time:3e3,shade:!1,fix:!1,maxWidth:210},n))}},r=function(e){var t=this;t.index=++o.index,t.config=i.extend({},t.config,a.config,e),t.creat()};r.pt=r.prototype;var l=["layui-layer",".layui-layer-title",".layui-layer-main",".layui-layer-dialog","layui-layer-iframe","layui-layer-content","layui-layer-btn","layui-layer-close"];l.anim=["layer-anim","layer-anim-01","layer-anim-02","layer-anim-03","layer-anim-04","layer-anim-05","layer-anim-06"],r.pt.config={type:0,shade:.3,fix:!0,move:l[1],title:"&#x4FE1;&#x606F;",offset:"auto",area:"auto",closeBtn:1,time:0,zIndex:19891014,maxWidth:360,shift:0,icon:-1,scrollbar:!0,tips:2},r.pt.vessel=function(e,t){var i=this,n=i.index,o=i.config,r=o.zIndex+n,s="object"==typeof o.title,c=o.maxmin&&(1===o.type||2===o.type),f=o.title?'<div class="layui-layer-title" style="'+(s?o.title[1]:"")+'">'+(s?o.title[0]:o.title)+"</div>":"";return o.zIndex=r,t([o.shade?'<div class="layui-layer-shade" id="layui-layer-shade'+n+'" times="'+n+'" style="'+("z-index:"+(r-1)+"; background-color:"+(o.shade[1]||"#000")+"; opacity:"+(o.shade[0]||o.shade)+"; filter:alpha(opacity="+(100*o.shade[0]||100*o.shade)+");")+'"></div>':"",'<div class="'+l[0]+(" layui-layer-"+a.type[o.type])+(0!=o.type&&2!=o.type||o.shade?"":" layui-layer-border")+" "+(o.skin||"")+'" id="'+l[0]+n+'" type="'+a.type[o.type]+'" times="'+n+'" showtime="'+o.time+'" conType="'+(e?"object":"string")+'" style="z-index: '+r+"; width:"+o.area[0]+";height:"+o.area[1]+(o.fix?"":";position:absolute;")+'">'+(e&&2!=o.type?"":f)+'<div id="'+(o.id||"")+'" class="layui-layer-content'+(0==o.type&&o.icon!==-1?" layui-layer-padding":"")+(3==o.type?" layui-layer-loading"+o.icon:"")+'">'+(0==o.type&&o.icon!==-1?'<i class="layui-layer-ico layui-layer-ico'+o.icon+'"></i>':"")+(1==o.type&&e?"":o.content||"")+'</div><span class="layui-layer-setwin">'+function(){var e=c?'<a class="layui-layer-min" href="javascript:;"><cite></cite></a><a class="layui-layer-ico layui-layer-max" href="javascript:;"></a>':"";return o.closeBtn&&(e+='<a class="layui-layer-ico '+l[7]+" "+l[7]+(o.title?o.closeBtn:4==o.type?"1":"2")+'" href="javascript:;"></a>'),e}()+"</span>"+(o.btn?function(){var e="";"string"==typeof o.btn&&(o.btn=[o.btn]);for(var t=0,i=o.btn.length;t<i;t++)e+='<a class="'+l[6]+t+'">'+o.btn[t]+"</a>";return'<div class="'+l[6]+'">'+e+"</div>"}():"")+"</div>"],f),i},r.pt.creat=function(){var e=this,t=e.config,r=e.index,s=t.content,c="object"==typeof s;if(!i("#"+t.id)[0]){switch("string"==typeof t.area&&(t.area="auto"===t.area?["",""]:[t.area,""]),t.type){case 0:t.btn="btn"in t?t.btn:a.btn[0],o.closeAll("dialog");break;case 2:var s=t.content=c?t.content:[t.content||"http://layer.layui.com","auto"];t.content='<iframe scrolling="'+(t.content[1]||"auto")+'" allowtransparency="true" id="'+l[4]+r+'" name="'+l[4]+r+'" onload="this.className=\'\';" class="layui-layer-load" frameborder="0" src="'+t.content[0]+'"></iframe>';break;case 3:t.title=!1,t.closeBtn=!1,t.icon===-1&&0===t.icon,o.closeAll("loading");break;case 4:c||(t.content=[t.content,"body"]),t.follow=t.content[1],t.content=t.content[0]+'<i class="layui-layer-TipsG"></i>',t.title=!1,t.tips="object"==typeof t.tips?t.tips:[t.tips,!0],t.tipsMore||o.closeAll("tips")}e.vessel(c,function(n,a){i("body").append(n[0]),c?function(){2==t.type||4==t.type?function(){i("body").append(n[1])}():function(){s.parents("."+l[0])[0]||(s.show().addClass("layui-layer-wrap").wrap(n[1]),i("#"+l[0]+r).find("."+l[5]).before(a))}()}():i("body").append(n[1]),e.layero=i("#"+l[0]+r),t.scrollbar||l.html.css("overflow","hidden").attr("layer-full",r)}).auto(r),2==t.type&&o.ie6&&e.layero.find("iframe").attr("src",s[0]),i(document).off("keydown",a.enter).on("keydown",a.enter),e.layero.on("keydown",function(e){i(document).off("keydown",a.enter)}),4==t.type?e.tips():e.offset(),t.fix&&n.on("resize",function(){e.offset(),(/^\d+%$/.test(t.area[0])||/^\d+%$/.test(t.area[1]))&&e.auto(r),4==t.type&&e.tips()}),t.time<=0||setTimeout(function(){o.close(e.index)},t.time),e.move().callback(),l.anim[t.shift]&&e.layero.addClass(l.anim[t.shift])}},r.pt.auto=function(e){function t(e){e=r.find(e),e.height(s[1]-c-f-2*(0|parseFloat(e.css("padding"))))}var a=this,o=a.config,r=i("#"+l[0]+e);""===o.area[0]&&o.maxWidth>0&&(/MSIE 7/.test(navigator.userAgent)&&o.btn&&r.width(r.innerWidth()),r.outerWidth()>o.maxWidth&&r.width(o.maxWidth));var s=[r.innerWidth(),r.innerHeight()],c=r.find(l[1]).outerHeight()||0,f=r.find("."+l[6]).outerHeight()||0;switch(o.type){case 2:t("iframe");break;default:""===o.area[1]?o.fix&&s[1]>=n.height()&&(s[1]=n.height(),t("."+l[5])):t("."+l[5])}return a},r.pt.offset=function(){var e=this,t=e.config,i=e.layero,a=[i.outerWidth(),i.outerHeight()],o="object"==typeof t.offset;e.offsetTop=(n.height()-a[1])/2,e.offsetLeft=(n.width()-a[0])/2,o?(e.offsetTop=t.offset[0],e.offsetLeft=t.offset[1]||e.offsetLeft):"auto"!==t.offset&&(e.offsetTop=t.offset,"rb"===t.offset&&(e.offsetTop=n.height()-a[1],e.offsetLeft=n.width()-a[0])),t.fix||(e.offsetTop=/%$/.test(e.offsetTop)?n.height()*parseFloat(e.offsetTop)/100:parseFloat(e.offsetTop),e.offsetLeft=/%$/.test(e.offsetLeft)?n.width()*parseFloat(e.offsetLeft)/100:parseFloat(e.offsetLeft),e.offsetTop+=n.scrollTop(),e.offsetLeft+=n.scrollLeft()),i.css({top:e.offsetTop,left:e.offsetLeft})},r.pt.tips=function(){var e=this,t=e.config,a=e.layero,o=[a.outerWidth(),a.outerHeight()],r=i(t.follow);r[0]||(r=i("body"));var s={width:r.outerWidth(),height:r.outerHeight(),top:r.offset().top,left:r.offset().left},c=a.find(".layui-layer-TipsG"),f=t.tips[0];t.tips[1]||c.remove(),s.autoLeft=function(){s.left+o[0]-n.width()>0?(s.tipLeft=s.left+s.width-o[0],c.css({right:30,left:"auto"})):s.tipLeft=s.left},s.where=[function(){s.autoLeft(),s.tipTop=s.top-o[1]-10,c.removeClass("layui-layer-TipsB").addClass("layui-layer-TipsT").css("border-right-color",t.tips[1])},function(){s.tipLeft=s.left+s.width+10,s.tipTop=s.top,c.removeClass("layui-layer-TipsL").addClass("layui-layer-TipsR").css("border-bottom-color",t.tips[1])},function(){s.autoLeft(),s.tipTop=s.top+s.height+10,c.removeClass("layui-layer-TipsT").addClass("layui-layer-TipsB").css("border-right-color",t.tips[1])},function(){s.tipLeft=s.left-o[0]-10,s.tipTop=s.top,c.removeClass("layui-layer-TipsR").addClass("layui-layer-TipsL").css("border-bottom-color",t.tips[1])}],s.where[f-1](),1===f?s.top-(n.scrollTop()+o[1]+16)<0&&s.where[2]():2===f?n.width()-(s.left+s.width+o[0]+16)>0||s.where[3]():3===f?s.top-n.scrollTop()+s.height+o[1]+16-n.height()>0&&s.where[0]():4===f&&o[0]+16-s.left>0&&s.where[1](),a.find("."+l[5]).css({"background-color":t.tips[1],"padding-right":t.closeBtn?"30px":""}),a.css({left:s.tipLeft-(t.fix?n.scrollLeft():0),top:s.tipTop-(t.fix?n.scrollTop():0)})},r.pt.move=function(){var e=this,t=e.config,a={setY:0,moveLayer:function(){var e=a.layero,t=parseInt(e.css("margin-left")),i=parseInt(a.move.css("left"));0===t||(i-=t),"fixed"!==e.css("position")&&(i-=e.parent().offset().left,a.setY=0),e.css({left:i,top:parseInt(a.move.css("top"))-a.setY})}},o=e.layero.find(t.move);return t.move&&o.attr("move","ok"),o.css({cursor:t.move?"move":"auto"}),i(t.move).on("mousedown",function(e){if(e.preventDefault(),"ok"===i(this).attr("move")){a.ismove=!0,a.layero=i(this).parents("."+l[0]);var o=a.layero.offset().left,r=a.layero.offset().top,s=a.layero.outerWidth()-6,c=a.layero.outerHeight()-6;i("#layui-layer-moves")[0]||i("body").append('<div id="layui-layer-moves" class="layui-layer-moves" style="left:'+o+"px; top:"+r+"px; width:"+s+"px; height:"+c+'px; z-index:2147483584"></div>'),a.move=i("#layui-layer-moves"),t.moveType&&a.move.css({visibility:"hidden"}),a.moveX=e.pageX-a.move.position().left,a.moveY=e.pageY-a.move.position().top,"fixed"!==a.layero.css("position")||(a.setY=n.scrollTop())}}),i(document).mousemove(function(e){if(a.ismove){var i=e.pageX-a.moveX,o=e.pageY-a.moveY;if(e.preventDefault(),!t.moveOut){a.setY=n.scrollTop();var r=n.width()-a.move.outerWidth(),l=a.setY;i<0&&(i=0),i>r&&(i=r),o<l&&(o=l),o>n.height()-a.move.outerHeight()+a.setY&&(o=n.height()-a.move.outerHeight()+a.setY)}a.move.css({left:i,top:o}),t.moveType&&a.moveLayer(),i=o=r=l=null}}).mouseup(function(){try{a.ismove&&(a.moveLayer(),a.move.remove(),t.moveEnd&&t.moveEnd()),a.ismove=!1}catch(e){a.ismove=!1}}),e},r.pt.callback=function(){function e(){var e=r.cancel&&r.cancel(t.index,n);e===!1||o.close(t.index)}var t=this,n=t.layero,r=t.config;t.openLayer(),r.type==2&&typeof r.iframeSrcChange==="function"&&n.find("iframe").on("load",function(){r.iframeSrcChange(n,t.index)}),r.success&&(2==r.type?n.find("iframe").on("load",function(){r.success(n,t.index)}):r.success(n,t.index)),o.ie6&&t.IE6(n),n.find("."+l[6]).children("a").on("click",function(){var e=i(this).index();if(0===e)r.yes?r.yes(t.index,n):r.btn1?r.btn1(t.index,n):o.close(t.index);else{var a=r["btn"+(e+1)]&&r["btn"+(e+1)](t.index,n);a===!1||o.close(t.index)}}),n.find("."+l[7]).on("click",e),r.shadeClose&&i("#layui-layer-shade"+t.index).on("click",function(){o.close(t.index)}),n.find(".layui-layer-min").on("click",function(){var e=r.min&&r.min(n);e===!1||o.min(t.index,r)}),n.find(".layui-layer-max").on("click",function(){i(this).hasClass("layui-layer-maxmin")?(o.restore(t.index),r.restore&&r.restore(n)):(o.full(t.index,r),setTimeout(function(){r.full&&r.full(n)},100))}),r.end&&(a.end[t.index]=r.end)},a.reselect=function(){i.each(i("select"),function(e,t){var n=i(this);n.parents("."+l[0])[0]||1==n.attr("layer")&&i("."+l[0]).length<1&&n.removeAttr("layer").show(),n=null})},r.pt.IE6=function(e){function t(){e.css({top:o+(a.config.fix?n.scrollTop():0)})}var a=this,o=e.offset().top;t(),n.scroll(t),i("select").each(function(e,t){var n=i(this);n.parents("."+l[0])[0]||"none"===n.css("display")||n.attr({layer:"1"}).hide(),n=null})},r.pt.openLayer=function(){var e=this;o.zIndex=e.config.zIndex,o.setTop=function(e){var t=function(){o.zIndex++,e.css("z-index",o.zIndex+1)};return o.zIndex=parseInt(e[0].style.zIndex),e.on("mousedown",t),o.zIndex}},a.record=function(e){var t=[e.width(),e.height(),e.position().top,e.position().left+parseFloat(e.css("margin-left"))];e.find(".layui-layer-max").addClass("layui-layer-maxmin"),e.attr({area:t})},a.rescollbar=function(e){l.html.attr("layer-full")==e&&(l.html[0].style.removeProperty?l.html[0].style.removeProperty("overflow"):l.html[0].style.removeAttribute("overflow"),l.html.removeAttr("layer-full"))},e.layer=o,o.getChildFrame=function(e,t){return t=t||i("."+l[4]).attr("times"),i("#"+l[0]+t).find("iframe").contents().find(e)},o.getFrameIndex=function(e){return i("#"+e).parents("."+l[4]).attr("times")},o.iframeAuto=function(e){if(e){var t=o.getChildFrame("html",e).outerHeight(),n=i("#"+l[0]+e),a=n.find(l[1]).outerHeight()||0,r=n.find("."+l[6]).outerHeight()||0;n.css({height:t+a+r}),n.find("iframe").css({height:t})}},o.iframeSrc=function(e,t){i("#"+l[0]+e).find("iframe").attr("src",t)},o.style=function(e,t){var n=i("#"+l[0]+e),o=n.attr("type"),r=n.find(l[1]).outerHeight()||0,s=n.find("."+l[6]).outerHeight()||0;o!==a.type[1]&&o!==a.type[2]||(n.css(t),o===a.type[2]&&n.find("iframe").css({height:parseFloat(t.height)-r-s}))},o.min=function(e,t){var n=i("#"+l[0]+e),r=n.find(l[1]).outerHeight()||0;a.record(n),o.style(e,{width:180,height:r,overflow:"hidden"}),n.find(".layui-layer-min").hide(),"page"===n.attr("type")&&n.find(l[4]).hide(),a.rescollbar(e)},o.restore=function(e){var t=i("#"+l[0]+e),n=t.attr("area").split(",");t.attr("type");o.style(e,{width:parseFloat(n[0]),height:parseFloat(n[1]),top:parseFloat(n[2]),left:parseFloat(n[3]),overflow:"visible"}),t.find(".layui-layer-max").removeClass("layui-layer-maxmin"),t.find(".layui-layer-min").show(),"page"===t.attr("type")&&t.find(l[4]).show(),a.rescollbar(e)},o.full=function(e){var t,r=i("#"+l[0]+e);a.record(r),l.html.attr("layer-full")||l.html.css("overflow","hidden").attr("layer-full",e),clearTimeout(t),t=setTimeout(function(){var t="fixed"===r.css("position");o.style(e,{top:t?0:n.scrollTop(),left:t?0:n.scrollLeft(),width:n.width(),height:n.height()}),r.find(".layui-layer-min").hide()},100)},o.title=function(e,t){var n=i("#"+l[0]+(t||o.index)).find(l[1]);n.html(e)},o.close=function(e){var t=i("#"+l[0]+e),n=t.attr("type");if(t[0]){if(n===a.type[1]&&"object"===t.attr("conType")){t.children(":not(."+l[5]+")").remove();for(var r=0;r<2;r++)t.find(".layui-layer-wrap").unwrap().hide()}else{if(n===a.type[2])try{var s=i("#"+l[4]+e)[0];s.contentWindow.document.write(""),s.contentWindow.close(),t.find("."+l[5])[0].removeChild(s)}catch(c){}t[0].innerHTML="",t.remove()}i("#layui-layer-moves, #layui-layer-shade"+e).remove(),o.ie6&&a.reselect(),a.rescollbar(e),i(document).off("keydown",a.enter),"function"==typeof a.end[e]&&a.end[e](),delete a.end[e]}},o.closeAll=function(e){i.each(i("."+l[0]),function(){var t=i(this),n=e?t.attr("type")===e:1;n&&o.close(t.attr("times")),n=null})};var s=o.cache||{},c=function(e){return s.skin?" "+s.skin+" "+s.skin+"-"+e:""};o.prompt=function(e,t){e=e||{},"function"==typeof e&&(t=e);var n,a=2==e.formType?'<textarea class="layui-layer-input">'+(e.value||"")+"</textarea>":function(){return'<input type="'+(1==e.formType?"password":"text")+'" class="layui-layer-input" value="'+(e.value||"")+'">'}();return o.open(i.extend({btn:["&#x786E;&#x5B9A;","&#x53D6;&#x6D88;"],content:a,skin:"layui-layer-prompt"+c("prompt"),success:function(e){n=e.find(".layui-layer-input"),n.focus()},yes:function(i){var a=n.val();""===a?n.focus():a.length>(e.maxlength||500)?o.tips("&#x6700;&#x591A;&#x8F93;&#x5165;"+(e.maxlength||500)+"&#x4E2A;&#x5B57;&#x6570;",n,{tips:1}):t&&t(a,i,n)}},e))},o.tab=function(e){e=e||{};var t=e.tab||{};return o.open(i.extend({type:1,skin:"layui-layer-tab"+c("tab"),title:function(){var e=t.length,i=1,n="";if(e>0)for(n='<span class="layui-layer-tabnow">'+t[0].title+"</span>";i<e;i++)n+="<span>"+t[i].title+"</span>";return n}(),content:'<ul class="layui-layer-tabmain">'+function(){var e=t.length,i=1,n="";if(e>0)for(n='<li class="layui-layer-tabli xubox_tab_layer">'+(t[0].content||"no content")+"</li>";i<e;i++)n+='<li class="layui-layer-tabli">'+(t[i].content||"no  content")+"</li>";return n}()+"</ul>",success:function(t){var n=t.find(".layui-layer-title").children(),a=t.find(".layui-layer-tabmain").children();n.on("mousedown",function(t){t.stopPropagation?t.stopPropagation():t.cancelBubble=!0;var n=i(this),o=n.index();n.addClass("layui-layer-tabnow").siblings().removeClass("layui-layer-tabnow"),a.eq(o).show().siblings().hide(),"function"==typeof e.change&&e.change(o)})}},e))},o.photos=function(t,n,a){function r(e,t,i){var n=new Image;return n.src=e,n.complete?t(n):(n.onload=function(){n.onload=null,t(n)},void(n.onerror=function(e){n.onerror=null,i(e)}))}var l={};if(t=t||{},t.photos){var s=t.photos.constructor===Object,f=s?t.photos:{},u=f.data||[],d=f.start||0;if(l.imgIndex=(0|d)+1,t.img=t.img||"img",s){if(0===u.length)return o.msg("&#x6CA1;&#x6709;&#x56FE;&#x7247;")}else{var y=i(t.photos),p=function(){u=[],y.find(t.img).each(function(e){var t=i(this);t.attr("layer-index",e),u.push({alt:t.attr("alt"),pid:t.attr("layer-pid"),src:t.attr("layer-src")||t.attr("src"),thumb:t.attr("src")})})};if(p(),0===u.length)return;if(n||y.on("click",t.img,function(){var e=i(this),n=e.attr("layer-index");o.photos(i.extend(t,{photos:{start:n,data:u,tab:t.tab},full:t.full}),!0),p()}),!n)return}l.imgprev=function(e){l.imgIndex--,l.imgIndex<1&&(l.imgIndex=u.length),l.tabimg(e)},l.imgnext=function(e,t){l.imgIndex++,l.imgIndex>u.length&&(l.imgIndex=1,t)||l.tabimg(e)},l.keyup=function(e){if(!l.end){var t=e.keyCode;e.preventDefault(),37===t?l.imgprev(!0):39===t?l.imgnext(!0):27===t&&o.close(l.index)}},l.tabimg=function(e){u.length<=1||(f.start=l.imgIndex-1,o.close(l.index),o.photos(t,!0,e))},l.event=function(){l.bigimg.hover(function(){l.imgsee.show()},function(){l.imgsee.hide()}),l.bigimg.find(".layui-layer-imgprev").on("click",function(e){e.preventDefault(),l.imgprev()}),l.bigimg.find(".layui-layer-imgnext").on("click",function(e){e.preventDefault(),l.imgnext()}),i(document).on("keyup",l.keyup)},l.loadi=o.load(1,{shade:!("shade"in t)&&.9,scrollbar:!1}),r(u[d].src,function(n){o.close(l.loadi),l.index=o.open(i.extend({type:1,area:function(){var a=[n.width,n.height],o=[i(e).width()-50,i(e).height()-50];return!t.full&&a[0]>o[0]&&(a[0]=o[0],a[1]=a[0]*n.height/n.width),[a[0]+"px",a[1]+"px"]}(),title:!1,shade:.9,shadeClose:!0,closeBtn:!1,move:".layui-layer-phimg img",moveType:1,scrollbar:!1,moveOut:!0,shift:5*Math.random()|0,skin:"layui-layer-photos"+c("photos"),content:'<div class="layui-layer-phimg"><img src="'+u[d].src+'" alt="'+(u[d].alt||"")+'" layer-pid="'+u[d].pid+'"><div class="layui-layer-imgsee">'+(u.length>1?'<span class="layui-layer-imguide"><a href="javascript:;" class="layui-layer-iconext layui-layer-imgprev"></a><a href="javascript:;" class="layui-layer-iconext layui-layer-imgnext"></a></span>':"")+'<div class="layui-layer-imgbar" style="display:'+(a?"block":"")+'"><span class="layui-layer-imgtit"><a href="javascript:;">'+(u[d].alt||"")+"</a><em>"+l.imgIndex+"/"+u.length+"</em></span></div></div></div>",success:function(e,i){l.bigimg=e.find(".layui-layer-phimg"),l.imgsee=e.find(".layui-layer-imguide,.layui-layer-imgbar"),l.event(e),t.tab&&t.tab(u[d],e)},end:function(){l.end=!0,i(document).off("keyup",l.keyup)}},t))},function(){o.close(l.loadi),o.msg("&#x5F53;&#x524D;&#x56FE;&#x7247;&#x5730;&#x5740;&#x5F02;&#x5E38;<br>&#x662F;&#x5426;&#x7EE7;&#x7EED;&#x67E5;&#x770B;&#x4E0B;&#x4E00;&#x5F20;&#xFF1F;",{time:3e4,btn:["&#x4E0B;&#x4E00;&#x5F20;","&#x4E0D;&#x770B;&#x4E86;"],yes:function(){u.length>1&&l.imgnext(!0,!0)}})})}},a.run=function(){i=jQuery,n=i(e),l.html=i("html"),o.open=function(e){var t=new r(e);return t.index}},"function"==typeof define?define(function(){return a.run(),o}):function(){a.run(),o.use("skin/default/layer.css")}()}(window);

;/*!src/static/lib/laydate.js*/
/*! laydate-v5.0.9 日期与时间组件 MIT License  http://www.layui.com/laydate/  By 贤心 */
 ;!function(){"use strict";var e=window.layui&&layui.define,t={getPath:function(){var e=document.currentScript?document.currentScript.src:function(){for(var e,t=document.scripts,n=t.length-1,a=n;a>0;a--)if("interactive"===t[a].readyState){e=t[a].src;break}return e||t[n].src}();return e.substring(0,e.lastIndexOf("/")+1)}(),getStyle:function(e,t){var n=e.currentStyle?e.currentStyle:window.getComputedStyle(e,null);return n[n.getPropertyValue?"getPropertyValue":"getAttribute"](t)},link:function(e,a,i){if(n.path){var r=document.getElementsByTagName("head")[0],o=document.createElement("link");"string"==typeof a&&(i=a);var s=(i||e).replace(/\.|\//g,""),l="layuicss-"+s,d=0;o.rel="stylesheet",o.href=n.path+e,o.id=l,document.getElementById(l)||r.appendChild(o),"function"==typeof a&&!function c(){return++d>80?window.console&&console.error("laydate.css: Invalid"):void(1989===parseInt(t.getStyle(document.getElementById(l),"width"))?a():setTimeout(c,100))}()}}},n={v:"5.0.9",config:{},index:window.laydate&&window.laydate.v?1e5:0,path:t.getPath,set:function(e){var t=this;return t.config=w.extend({},t.config,e),t},ready:function(a){var i="laydate",r="",o=(e?"modules/laydate/":"theme/")+"default/laydate.css?v="+n.v+r;return e?layui.addcss(o,a,i):t.link(o,a,i),this}},a=function(){var e=this;return{hint:function(t){e.hint.call(e,t)},config:e.config}},i="laydate",r=".layui-laydate",o="layui-this",s="laydate-disabled",l="开始日期超出了结束日期<br>建议重新选择",d=[100,2e5],c="layui-laydate-static",m="layui-laydate-list",u="laydate-selected",h="layui-laydate-hint",y="laydate-day-prev",f="laydate-day-next",p="layui-laydate-footer",g=".laydate-btns-confirm",v="laydate-time-text",D=".laydate-btns-time",T=function(e){var t=this;t.index=++n.index,t.config=w.extend({},t.config,n.config,e),n.ready(function(){t.init()})},w=function(e){return new C(e)},C=function(e){for(var t=0,n="object"==typeof e?[e]:(this.selector=e,document.querySelectorAll(e||null));t<n.length;t++)this.push(n[t])};C.prototype=[],C.prototype.constructor=C,w.extend=function(){var e=1,t=arguments,n=function(e,t){e=e||(t.constructor===Array?[]:{});for(var a in t)e[a]=t[a]&&t[a].constructor===Object?n(e[a],t[a]):t[a];return e};for(t[0]="object"==typeof t[0]?t[0]:{};e<t.length;e++)"object"==typeof t[e]&&n(t[0],t[e]);return t[0]},w.ie=function(){var e=navigator.userAgent.toLowerCase();return!!(window.ActiveXObject||"ActiveXObject"in window)&&((e.match(/msie\s(\d+)/)||[])[1]||"11")}(),w.stope=function(e){e=e||window.event,e.stopPropagation?e.stopPropagation():e.cancelBubble=!0},w.each=function(e,t){var n,a=this;if("function"!=typeof t)return a;if(e=e||[],e.constructor===Object){for(n in e)if(t.call(e[n],n,e[n]))break}else for(n=0;n<e.length&&!t.call(e[n],n,e[n]);n++);return a},w.digit=function(e,t,n){var a="";e=String(e),t=t||2;for(var i=e.length;i<t;i++)a+="0";return e<Math.pow(10,t)?a+(0|e):e},w.elem=function(e,t){var n=document.createElement(e);return w.each(t||{},function(e,t){n.setAttribute(e,t)}),n},C.addStr=function(e,t){return e=e.replace(/\s+/," "),t=t.replace(/\s+/," ").split(" "),w.each(t,function(t,n){new RegExp("\\b"+n+"\\b").test(e)||(e=e+" "+n)}),e.replace(/^\s|\s$/,"")},C.removeStr=function(e,t){return e=e.replace(/\s+/," "),t=t.replace(/\s+/," ").split(" "),w.each(t,function(t,n){var a=new RegExp("\\b"+n+"\\b");a.test(e)&&(e=e.replace(a,""))}),e.replace(/\s+/," ").replace(/^\s|\s$/,"")},C.prototype.find=function(e){var t=this,n=0,a=[],i="object"==typeof e;return this.each(function(r,o){for(var s=i?[e]:o.querySelectorAll(e||null);n<s.length;n++)a.push(s[n]);t.shift()}),i||(t.selector=(t.selector?t.selector+" ":"")+e),w.each(a,function(e,n){t.push(n)}),t},C.prototype.each=function(e){return w.each.call(this,this,e)},C.prototype.addClass=function(e,t){return this.each(function(n,a){a.className=C[t?"removeStr":"addStr"](a.className,e)})},C.prototype.removeClass=function(e){return this.addClass(e,!0)},C.prototype.hasClass=function(e){var t=!1;return this.each(function(n,a){new RegExp("\\b"+e+"\\b").test(a.className)&&(t=!0)}),t},C.prototype.attr=function(e,t){var n=this;return void 0===t?function(){if(n.length>0)return n[0].getAttribute(e)}():n.each(function(n,a){a.setAttribute(e,t)})},C.prototype.removeAttr=function(e){return this.each(function(t,n){n.removeAttribute(e)})},C.prototype.html=function(e){return this.each(function(t,n){n.innerHTML=e})},C.prototype.val=function(e){return this.each(function(t,n){n.value=e})},C.prototype.append=function(e){return this.each(function(t,n){"object"==typeof e?n.appendChild(e):n.innerHTML=n.innerHTML+e})},C.prototype.remove=function(e){return this.each(function(t,n){e?n.removeChild(e):n.parentNode.removeChild(n)})},C.prototype.on=function(e,t){return this.each(function(n,a){a.attachEvent?a.attachEvent("on"+e,function(e){e.target=e.srcElement,t.call(a,e)}):a.addEventListener(e,t,!1)})},C.prototype.off=function(e,t){return this.each(function(n,a){a.detachEvent?a.detachEvent("on"+e,t):a.removeEventListener(e,t,!1)})},T.isLeapYear=function(e){return e%4===0&&e%100!==0||e%400===0},T.prototype.config={type:"date",range:!1,format:"yyyy-MM-dd",value:null,min:"1900-1-1",max:"2099-12-31",trigger:"focus",show:!1,showBottom:!0,btns:["clear","now","confirm"],lang:"cn",theme:"default",position:null,calendar:!1,mark:{},zIndex:null,done:null,change:null},T.prototype.lang=function(){var e=this,t=e.config,n={cn:{weeks:["日","一","二","三","四","五","六"],time:["时","分","秒"],timeTips:"选择时间",startTime:"开始时间",endTime:"结束时间",dateTips:"返回日期",month:["一","二","三","四","五","六","七","八","九","十","十一","十二"],tools:{confirm:"确定",clear:"清空",now:"现在"}},en:{weeks:["Su","Mo","Tu","We","Th","Fr","Sa"],time:["Hours","Minutes","Seconds"],timeTips:"Select Time",startTime:"Start Time",endTime:"End Time",dateTips:"Select Date",month:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],tools:{confirm:"Confirm",clear:"Clear",now:"Now"}}};return n[t.lang]||n.cn},T.prototype.init=function(){var e=this,t=e.config,n="yyyy|y|MM|M|dd|d|HH|H|mm|m|ss|s",a="static"===t.position,i={year:"yyyy",month:"yyyy-MM",date:"yyyy-MM-dd",time:"HH:mm:ss",datetime:"yyyy-MM-dd HH:mm:ss"};t.elem=w(t.elem),t.eventElem=w(t.eventElem),t.elem[0]&&(t.range===!0&&(t.range="-"),t.format===i.date&&(t.format=i[t.type]),e.format=t.format.match(new RegExp(n+"|.","g"))||[],e.EXP_IF="",e.EXP_SPLIT="",w.each(e.format,function(t,a){var i=new RegExp(n).test(a)?"\\d{"+function(){return new RegExp(n).test(e.format[0===t?t+1:t-1]||"")?/^yyyy|y$/.test(a)?4:a.length:/^yyyy$/.test(a)?"1,4":/^y$/.test(a)?"1,308":"1,2"}()+"}":"\\"+a;e.EXP_IF=e.EXP_IF+i,e.EXP_SPLIT=e.EXP_SPLIT+"("+i+")"}),e.EXP_IF=new RegExp("^"+(t.range?e.EXP_IF+"\\s\\"+t.range+"\\s"+e.EXP_IF:e.EXP_IF)+"$"),e.EXP_SPLIT=new RegExp("^"+e.EXP_SPLIT+"$",""),e.isInput(t.elem[0])||"focus"===t.trigger&&(t.trigger="click"),t.elem.attr("lay-key")||(t.elem.attr("lay-key",e.index),t.eventElem.attr("lay-key",e.index)),t.mark=w.extend({},t.calendar&&"cn"===t.lang?{"0-1-1":"元旦","0-2-14":"情人","0-3-8":"妇女","0-3-12":"植树","0-4-1":"愚人","0-5-1":"劳动","0-5-4":"青年","0-6-1":"儿童","0-9-10":"教师","0-9-18":"国耻","0-10-1":"国庆","0-12-25":"圣诞"}:{},t.mark),w.each(["min","max"],function(e,n){var a=[],i=[];if("number"==typeof t[n]){var r=t[n],o=(new Date).getTime(),s=864e5,l=new Date(r?r<s?o+r*s:r:o);a=[l.getFullYear(),l.getMonth()+1,l.getDate()],r<s||(i=[l.getHours(),l.getMinutes(),l.getSeconds()])}else a=(t[n].match(/\d+-\d+-\d+/)||[""])[0].split("-"),i=(t[n].match(/\d+:\d+:\d+/)||[""])[0].split(":");t[n]={year:0|a[0]||(new Date).getFullYear(),month:a[1]?(0|a[1])-1:(new Date).getMonth(),date:0|a[2]||(new Date).getDate(),hours:0|i[0],minutes:0|i[1],seconds:0|i[2]}}),e.elemID="layui-laydate"+t.elem.attr("lay-key"),(t.show||a)&&e.render(),a||e.events(),t.value&&(t.value.constructor===Date?e.setValue(e.parse(0,e.systemDate(t.value))):e.setValue(t.value)))},T.prototype.render=function(){var e=this,t=e.config,n=e.lang(),a="static"===t.position,i=e.elem=w.elem("div",{id:e.elemID,"class":["layui-laydate",t.range?" layui-laydate-range":"",a?" "+c:"",t.theme&&"default"!==t.theme&&!/^#/.test(t.theme)?" laydate-theme-"+t.theme:""].join("")}),r=e.elemMain=[],o=e.elemHeader=[],s=e.elemCont=[],l=e.table=[],d=e.footer=w.elem("div",{"class":p});if(t.zIndex&&(i.style.zIndex=t.zIndex),w.each(new Array(2),function(e){if(!t.range&&e>0)return!0;var a=w.elem("div",{"class":"layui-laydate-header"}),i=[function(){var e=w.elem("i",{"class":"layui-icon laydate-icon laydate-prev-y"});return e.innerHTML="&#xe65a;",e}(),function(){var e=w.elem("i",{"class":"layui-icon laydate-icon laydate-prev-m"});return e.innerHTML="&#xe603;",e}(),function(){var e=w.elem("div",{"class":"laydate-set-ym"}),t=w.elem("span"),n=w.elem("span");return e.appendChild(t),e.appendChild(n),e}(),function(){var e=w.elem("i",{"class":"layui-icon laydate-icon laydate-next-m"});return e.innerHTML="&#xe602;",e}(),function(){var e=w.elem("i",{"class":"layui-icon laydate-icon laydate-next-y"});return e.innerHTML="&#xe65b;",e}()],d=w.elem("div",{"class":"layui-laydate-content"}),c=w.elem("table"),m=w.elem("thead"),u=w.elem("tr");w.each(i,function(e,t){a.appendChild(t)}),m.appendChild(u),w.each(new Array(6),function(e){var t=c.insertRow(0);w.each(new Array(7),function(a){if(0===e){var i=w.elem("th");i.innerHTML=n.weeks[a],u.appendChild(i)}t.insertCell(a)})}),c.insertBefore(m,c.children[0]),d.appendChild(c),r[e]=w.elem("div",{"class":"layui-laydate-main laydate-main-list-"+e}),r[e].appendChild(a),r[e].appendChild(d),o.push(i),s.push(d),l.push(c)}),w(d).html(function(){var e=[],i=[];return"datetime"===t.type&&e.push('<span lay-type="datetime" class="laydate-btns-time">'+n.timeTips+"</span>"),w.each(t.btns,function(e,r){var o=n.tools[r]||"btn";t.range&&"now"===r||(a&&"clear"===r&&(o="cn"===t.lang?"重置":"Reset"),i.push('<span lay-type="'+r+'" class="laydate-btns-'+r+'">'+o+"</span>"))}),e.push('<div class="laydate-footer-btns">'+i.join("")+"</div>"),e.join("")}()),w.each(r,function(e,t){i.appendChild(t)}),t.showBottom&&i.appendChild(d),/^#/.test(t.theme)){var m=w.elem("style"),u=["#{{id}} .layui-laydate-header{background-color:{{theme}};}","#{{id}} .layui-this{background-color:{{theme}} !important;}"].join("").replace(/{{id}}/g,e.elemID).replace(/{{theme}}/g,t.theme);"styleSheet"in m?(m.setAttribute("type","text/css"),m.styleSheet.cssText=u):m.innerHTML=u,w(i).addClass("laydate-theme-molv"),i.appendChild(m)}e.remove(T.thisElemDate),a?t.elem.append(i):(document.body.appendChild(i),e.position()),e.checkDate().calendar(),e.changeEvent(),T.thisElemDate=e.elemID,"function"==typeof t.ready&&t.ready(w.extend({},t.dateTime,{month:t.dateTime.month+1}))},T.prototype.remove=function(e){var t=this,n=(t.config,w("#"+(e||t.elemID)));return n.hasClass(c)||t.checkDate(function(){n.remove()}),t},T.prototype.position=function(){var e=this,t=e.config,n=e.bindElem||t.elem[0],a=n.getBoundingClientRect(),i=e.elem.offsetWidth,r=e.elem.offsetHeight,o=function(e){return e=e?"scrollLeft":"scrollTop",document.body[e]|document.documentElement[e]},s=function(e){return document.documentElement[e?"clientWidth":"clientHeight"]},l=5,d=a.left,c=a.bottom;d+i+l>s("width")&&(d=s("width")-i-l),c+r+l>s()&&(c=a.top>r?a.top-r:s()-r,c-=2*l),t.position&&(e.elem.style.position=t.position),e.elem.style.left=d+("fixed"===t.position?0:o(1))+"px",e.elem.style.top=c+("fixed"===t.position?0:o())+"px"},T.prototype.hint=function(e){var t=this,n=(t.config,w.elem("div",{"class":h}));n.innerHTML=e||"",w(t.elem).find("."+h).remove(),t.elem.appendChild(n),clearTimeout(t.hinTimer),t.hinTimer=setTimeout(function(){w(t.elem).find("."+h).remove()},3e3)},T.prototype.getAsYM=function(e,t,n){return n?t--:t++,t<0&&(t=11,e--),t>11&&(t=0,e++),[e,t]},T.prototype.systemDate=function(e){var t=e||new Date;return{year:t.getFullYear(),month:t.getMonth(),date:t.getDate(),hours:e?e.getHours():0,minutes:e?e.getMinutes():0,seconds:e?e.getSeconds():0}},T.prototype.checkDate=function(e){var t,a,i=this,r=(new Date,i.config),o=r.dateTime=r.dateTime||i.systemDate(),s=i.bindElem||r.elem[0],l=(i.isInput(s)?"val":"html",i.isInput(s)?s.value:"static"===r.position?"":s.innerHTML),c=function(e){e.year>d[1]&&(e.year=d[1],a=!0),e.month>11&&(e.month=11,a=!0),e.hours>23&&(e.hours=0,a=!0),e.minutes>59&&(e.minutes=0,e.hours++,a=!0),e.seconds>59&&(e.seconds=0,e.minutes++,a=!0),t=n.getEndDate(e.month+1,e.year),e.date>t&&(e.date=t,a=!0)},m=function(e,t,n){var o=["startTime","endTime"];t=(t.match(i.EXP_SPLIT)||[]).slice(1),n=n||0,r.range&&(i[o[n]]=i[o[n]]||{}),w.each(i.format,function(s,l){var c=parseFloat(t[s]);t[s].length<l.length&&(a=!0),/yyyy|y/.test(l)?(c<d[0]&&(c=d[0],a=!0),e.year=c):/MM|M/.test(l)?(c<1&&(c=1,a=!0),e.month=c-1):/dd|d/.test(l)?(c<1&&(c=1,a=!0),e.date=c):/HH|H/.test(l)?(c<1&&(c=0,a=!0),e.hours=c,r.range&&(i[o[n]].hours=c)):/mm|m/.test(l)?(c<1&&(c=0,a=!0),e.minutes=c,r.range&&(i[o[n]].minutes=c)):/ss|s/.test(l)&&(c<1&&(c=0,a=!0),e.seconds=c,r.range&&(i[o[n]].seconds=c))}),c(e)};return"limit"===e?(c(o),i):(l=l||r.value,"string"==typeof l&&(l=l.replace(/\s+/g," ").replace(/^\s|\s$/g,"")),i.startState&&!i.endState&&(delete i.startState,i.endState=!0),"string"==typeof l&&l?i.EXP_IF.test(l)?r.range?(l=l.split(" "+r.range+" "),i.startDate=i.startDate||i.systemDate(),i.endDate=i.endDate||i.systemDate(),r.dateTime=w.extend({},i.startDate),w.each([i.startDate,i.endDate],function(e,t){m(t,l[e],e)})):m(o,l):(i.hint("日期格式不合法<br>必须遵循下述格式：<br>"+(r.range?r.format+" "+r.range+" "+r.format:r.format)+"<br>已为你重置"),a=!0):l&&l.constructor===Date?r.dateTime=i.systemDate(l):(r.dateTime=i.systemDate(),delete i.startState,delete i.endState,delete i.startDate,delete i.endDate,delete i.startTime,delete i.endTime),c(o),a&&l&&i.setValue(r.range?i.endDate?i.parse():"":i.parse()),e&&e(),i)},T.prototype.mark=function(e,t){var n,a=this,i=a.config;return w.each(i.mark,function(e,a){var i=e.split("-");i[0]!=t[0]&&0!=i[0]||i[1]!=t[1]&&0!=i[1]||i[2]!=t[2]||(n=a||t[2])}),n&&e.html('<span class="laydate-day-mark">'+n+"</span>"),a},T.prototype.limit=function(e,t,n,a){var i,r=this,o=r.config,l={},d=o[n>41?"endDate":"dateTime"],c=w.extend({},d,t||{});return w.each({now:c,min:o.min,max:o.max},function(e,t){l[e]=r.newDate(w.extend({year:t.year,month:t.month,date:t.date},function(){var e={};return w.each(a,function(n,a){e[a]=t[a]}),e}())).getTime()}),i=l.now<l.min||l.now>l.max,e&&e[i?"addClass":"removeClass"](s),i},T.prototype.calendar=function(e){var t,a,i,r=this,s=r.config,l=e||s.dateTime,c=new Date,m=r.lang(),u="date"!==s.type&&"datetime"!==s.type,h=e?1:0,y=w(r.table[h]).find("td"),f=w(r.elemHeader[h][2]).find("span");if(l.year<d[0]&&(l.year=d[0],r.hint("最低只能支持到公元"+d[0]+"年")),l.year>d[1]&&(l.year=d[1],r.hint("最高只能支持到公元"+d[1]+"年")),r.firstDate||(r.firstDate=w.extend({},l)),c.setFullYear(l.year,l.month,1),t=c.getDay(),a=n.getEndDate(l.month||12,l.year),i=n.getEndDate(l.month+1,l.year),w.each(y,function(e,n){var d=[l.year,l.month],c=0;n=w(n),n.removeAttr("class"),e<t?(c=a-t+e,n.addClass("laydate-day-prev"),d=r.getAsYM(l.year,l.month,"sub")):e>=t&&e<i+t?(c=e-t,s.range||c+1===l.date&&n.addClass(o)):(c=e-i-t,n.addClass("laydate-day-next"),d=r.getAsYM(l.year,l.month)),d[1]++,d[2]=c+1,n.attr("lay-ymd",d.join("-")).html(d[2]),r.mark(n,d).limit(n,{year:d[0],month:d[1]-1,date:d[2]},e)}),w(f[0]).attr("lay-ym",l.year+"-"+(l.month+1)),w(f[1]).attr("lay-ym",l.year+"-"+(l.month+1)),"cn"===s.lang?(w(f[0]).attr("lay-type","year").html(l.year+"年"),w(f[1]).attr("lay-type","month").html(l.month+1+"月")):(w(f[0]).attr("lay-type","month").html(m.month[l.month]),w(f[1]).attr("lay-type","year").html(l.year)),u&&(s.range&&(e?r.endDate=r.endDate||{year:l.year+("year"===s.type?1:0),month:l.month+("month"===s.type?0:-1)}:r.startDate=r.startDate||{year:l.year,month:l.month},e&&(r.listYM=[[r.startDate.year,r.startDate.month+1],[r.endDate.year,r.endDate.month+1]],r.list(s.type,0).list(s.type,1),"time"===s.type?r.setBtnStatus("时间",w.extend({},r.systemDate(),r.startTime),w.extend({},r.systemDate(),r.endTime)):r.setBtnStatus(!0))),s.range||(r.listYM=[[l.year,l.month+1]],r.list(s.type,0))),s.range&&!e){var p=r.getAsYM(l.year,l.month);r.calendar(w.extend({},l,{year:p[0],month:p[1]}))}return s.range||r.limit(w(r.footer).find(g),null,0,["hours","minutes","seconds"]),s.range&&e&&!u&&r.stampRange(),r},T.prototype.list=function(e,t){var n=this,a=n.config,i=a.dateTime,r=n.lang(),l=a.range&&"date"!==a.type&&"datetime"!==a.type,d=w.elem("ul",{"class":m+" "+{year:"laydate-year-list",month:"laydate-month-list",time:"laydate-time-list"}[e]}),c=n.elemHeader[t],u=w(c[2]).find("span"),h=n.elemCont[t||0],y=w(h).find("."+m)[0],f="cn"===a.lang,p=f?"年":"",T=n.listYM[t]||{},C=["hours","minutes","seconds"],x=["startTime","endTime"][t];if(T[0]<1&&(T[0]=1),"year"===e){var M,b=M=T[0]-7;b<1&&(b=M=1),w.each(new Array(15),function(e){var i=w.elem("li",{"lay-ym":M}),r={year:M};M==T[0]&&w(i).addClass(o),i.innerHTML=M+p,d.appendChild(i),M<n.firstDate.year?(r.month=a.min.month,r.date=a.min.date):M>=n.firstDate.year&&(r.month=a.max.month,r.date=a.max.date),n.limit(w(i),r,t),M++}),w(u[f?0:1]).attr("lay-ym",M-8+"-"+T[1]).html(b+p+" - "+(M-1+p))}else if("month"===e)w.each(new Array(12),function(e){var i=w.elem("li",{"lay-ym":e}),s={year:T[0],month:e};e+1==T[1]&&w(i).addClass(o),i.innerHTML=r.month[e]+(f?"月":""),d.appendChild(i),T[0]<n.firstDate.year?s.date=a.min.date:T[0]>=n.firstDate.year&&(s.date=a.max.date),n.limit(w(i),s,t)}),w(u[f?0:1]).attr("lay-ym",T[0]+"-"+T[1]).html(T[0]+p);else if("time"===e){var E=function(){w(d).find("ol").each(function(e,a){w(a).find("li").each(function(a,i){n.limit(w(i),[{hours:a},{hours:n[x].hours,minutes:a},{hours:n[x].hours,minutes:n[x].minutes,seconds:a}][e],t,[["hours"],["hours","minutes"],["hours","minutes","seconds"]][e])})}),a.range||n.limit(w(n.footer).find(g),n[x],0,["hours","minutes","seconds"])};a.range?n[x]||(n[x]={hours:0,minutes:0,seconds:0}):n[x]=i,w.each([24,60,60],function(e,t){var a=w.elem("li"),i=["<p>"+r.time[e]+"</p><ol>"];w.each(new Array(t),function(t){i.push("<li"+(n[x][C[e]]===t?' class="'+o+'"':"")+">"+w.digit(t,2)+"</li>")}),a.innerHTML=i.join("")+"</ol>",d.appendChild(a)}),E()}if(y&&h.removeChild(y),h.appendChild(d),"year"===e||"month"===e)w(n.elemMain[t]).addClass("laydate-ym-show"),w(d).find("li").on("click",function(){var r=0|w(this).attr("lay-ym");if(!w(this).hasClass(s)){if(0===t)i[e]=r,l&&(n.startDate[e]=r),n.limit(w(n.footer).find(g),null,0);else if(l)n.endDate[e]=r;else{var c="year"===e?n.getAsYM(r,T[1]-1,"sub"):n.getAsYM(T[0],r,"sub");w.extend(i,{year:c[0],month:c[1]})}"year"===a.type||"month"===a.type?(w(d).find("."+o).removeClass(o),w(this).addClass(o),"month"===a.type&&"year"===e&&(n.listYM[t][0]=r,l&&(n[["startDate","endDate"][t]].year=r),n.list("month",t))):(n.checkDate("limit").calendar(),n.closeList()),n.setBtnStatus(),a.range||n.done(null,"change"),w(n.footer).find(D).removeClass(s)}});else{var S=w.elem("span",{"class":v}),k=function(){w(d).find("ol").each(function(e){var t=this,a=w(t).find("li");t.scrollTop=30*(n[x][C[e]]-2),t.scrollTop<=0&&a.each(function(e,n){if(!w(this).hasClass(s))return t.scrollTop=30*(e-2),!0})})},H=w(c[2]).find("."+v);k(),S.innerHTML=a.range?[r.startTime,r.endTime][t]:r.timeTips,w(n.elemMain[t]).addClass("laydate-time-show"),H[0]&&H.remove(),c[2].appendChild(S),w(d).find("ol").each(function(e){var t=this;w(t).find("li").on("click",function(){var r=0|this.innerHTML;w(this).hasClass(s)||(a.range?n[x][C[e]]=r:i[C[e]]=r,w(t).find("."+o).removeClass(o),w(this).addClass(o),E(),k(),(n.endDate||"time"===a.type)&&n.done(null,"change"),n.setBtnStatus())})})}return n},T.prototype.listYM=[],T.prototype.closeList=function(){var e=this;e.config;w.each(e.elemCont,function(t,n){w(this).find("."+m).remove(),w(e.elemMain[t]).removeClass("laydate-ym-show laydate-time-show")}),w(e.elem).find("."+v).remove()},T.prototype.setBtnStatus=function(e,t,n){var a,i=this,r=i.config,o=w(i.footer).find(g),d=r.range&&"date"!==r.type&&"time"!==r.type;d&&(t=t||i.startDate,n=n||i.endDate,a=i.newDate(t).getTime()>i.newDate(n).getTime(),i.limit(null,t)||i.limit(null,n)?o.addClass(s):o[a?"addClass":"removeClass"](s),e&&a&&i.hint("string"==typeof e?l.replace(/日期/g,e):l))},T.prototype.parse=function(e,t){var n=this,a=n.config,i=t||(e?w.extend({},n.endDate,n.endTime):a.range?w.extend({},n.startDate,n.startTime):a.dateTime),r=n.format.concat();return w.each(r,function(e,t){/yyyy|y/.test(t)?r[e]=w.digit(i.year,t.length):/MM|M/.test(t)?r[e]=w.digit(i.month+1,t.length):/dd|d/.test(t)?r[e]=w.digit(i.date,t.length):/HH|H/.test(t)?r[e]=w.digit(i.hours,t.length):/mm|m/.test(t)?r[e]=w.digit(i.minutes,t.length):/ss|s/.test(t)&&(r[e]=w.digit(i.seconds,t.length))}),a.range&&!e?r.join("")+" "+a.range+" "+n.parse(1):r.join("")},T.prototype.newDate=function(e){return e=e||{},new Date(e.year||1,e.month||0,e.date||1,e.hours||0,e.minutes||0,e.seconds||0)},T.prototype.setValue=function(e){var t=this,n=t.config,a=t.bindElem||n.elem[0],i=t.isInput(a)?"val":"html";return"static"===n.position||w(a)[i](e||""),this},T.prototype.stampRange=function(){var e,t,n=this,a=n.config,i=w(n.elem).find("td");if(a.range&&!n.endDate&&w(n.footer).find(g).addClass(s),n.endDate)return e=n.newDate({year:n.startDate.year,month:n.startDate.month,date:n.startDate.date}).getTime(),t=n.newDate({year:n.endDate.year,month:n.endDate.month,date:n.endDate.date}).getTime(),e>t?n.hint(l):void w.each(i,function(a,i){var r=w(i).attr("lay-ymd").split("-"),s=n.newDate({year:r[0],month:r[1]-1,date:r[2]}).getTime();w(i).removeClass(u+" "+o),s!==e&&s!==t||w(i).addClass(w(i).hasClass(y)||w(i).hasClass(f)?u:o),s>e&&s<t&&w(i).addClass(u)})},T.prototype.done=function(e,t){var n=this,a=n.config,i=w.extend({},n.startDate?w.extend(n.startDate,n.startTime):a.dateTime),r=w.extend({},w.extend(n.endDate,n.endTime));return w.each([i,r],function(e,t){"month"in t&&w.extend(t,{month:t.month+1})}),e=e||[n.parse(),i,r],"function"==typeof a[t||"done"]&&a[t||"done"].apply(a,e),n},T.prototype.choose=function(e){var t=this,n=t.config,a=n.dateTime,i=w(t.elem).find("td"),r=e.attr("lay-ymd").split("-"),l=function(e){new Date;e&&w.extend(a,r),n.range&&(t.startDate?w.extend(t.startDate,r):t.startDate=w.extend({},r,t.startTime),t.startYMD=r)};if(r={year:0|r[0],month:(0|r[1])-1,date:0|r[2]},!e.hasClass(s))if(n.range){if(w.each(["startTime","endTime"],function(e,n){t[n]=t[n]||{hours:0,minutes:0,seconds:0}}),t.endState)l(),delete t.endState,delete t.endDate,t.startState=!0,i.removeClass(o+" "+u),e.addClass(o);else if(t.startState){if(e.addClass(o),t.endDate?w.extend(t.endDate,r):t.endDate=w.extend({},r,t.endTime),t.newDate(r).getTime()<t.newDate(t.startYMD).getTime()){var d=w.extend({},t.endDate,{hours:t.startDate.hours,minutes:t.startDate.minutes,seconds:t.startDate.seconds});w.extend(t.endDate,t.startDate,{hours:t.endDate.hours,minutes:t.endDate.minutes,seconds:t.endDate.seconds}),t.startDate=d}n.showBottom||t.done(),t.stampRange(),t.endState=!0,t.done(null,"change")}else e.addClass(o),l(),t.startState=!0;w(t.footer).find(g)[t.endDate?"removeClass":"addClass"](s)}else"static"===n.position?(l(!0),t.calendar().done().done(null,"change")):"date"===n.type?(l(!0),t.setValue(t.parse()).remove().done()):"datetime"===n.type&&(l(!0),t.calendar().done(null,"change"))},T.prototype.tool=function(e,t){var n=this,a=n.config,i=a.dateTime,r="static"===a.position,o={datetime:function(){w(e).hasClass(s)||(n.list("time",0),a.range&&n.list("time",1),w(e).attr("lay-type","date").html(n.lang().dateTips))},date:function(){n.closeList(),w(e).attr("lay-type","datetime").html(n.lang().timeTips)},clear:function(){n.setValue("").remove(),r&&(w.extend(i,n.firstDate),n.calendar()),a.range&&(delete n.startState,delete n.endState,delete n.endDate,delete n.startTime,delete n.endTime),n.done(["",{},{}])},now:function(){var e=new Date;w.extend(i,n.systemDate(),{hours:e.getHours(),minutes:e.getMinutes(),seconds:e.getSeconds()}),n.setValue(n.parse()).remove(),r&&n.calendar(),n.done()},confirm:function(){if(a.range){if(!n.endDate)return n.hint("请先选择日期范围");if(w(e).hasClass(s))return n.hint("time"===a.type?l.replace(/日期/g,"时间"):l)}else if(w(e).hasClass(s))return n.hint("不在有效日期或时间范围内");n.done(),n.setValue(n.parse()).remove()}};o[t]&&o[t]()},T.prototype.change=function(e){var t=this,n=t.config,a=n.dateTime,i=n.range&&("year"===n.type||"month"===n.type),r=t.elemCont[e||0],o=t.listYM[e],s=function(s){var l=["startDate","endDate"][e],d=w(r).find(".laydate-year-list")[0],c=w(r).find(".laydate-month-list")[0];return d&&(o[0]=s?o[0]-15:o[0]+15,t.list("year",e)),c&&(s?o[0]--:o[0]++,t.list("month",e)),(d||c)&&(w.extend(a,{year:o[0]}),i&&(t[l].year=o[0]),n.range||t.done(null,"change"),t.setBtnStatus(),n.range||t.limit(w(t.footer).find(g),{year:o[0]})),d||c};return{prevYear:function(){s("sub")||(a.year--,t.checkDate("limit").calendar(),n.range||t.done(null,"change"))},prevMonth:function(){var e=t.getAsYM(a.year,a.month,"sub");w.extend(a,{year:e[0],month:e[1]}),t.checkDate("limit").calendar(),n.range||t.done(null,"change")},nextMonth:function(){var e=t.getAsYM(a.year,a.month);w.extend(a,{year:e[0],month:e[1]}),t.checkDate("limit").calendar(),n.range||t.done(null,"change")},nextYear:function(){s()||(a.year++,t.checkDate("limit").calendar(),n.range||t.done(null,"change"))}}},T.prototype.changeEvent=function(){var e=this;e.config;w(e.elem).on("click",function(e){w.stope(e)}),w.each(e.elemHeader,function(t,n){w(n[0]).on("click",function(n){e.change(t).prevYear()}),w(n[1]).on("click",function(n){e.change(t).prevMonth()}),w(n[2]).find("span").on("click",function(n){var a=w(this),i=a.attr("lay-ym"),r=a.attr("lay-type");i&&(i=i.split("-"),e.listYM[t]=[0|i[0],0|i[1]],e.list(r,t),w(e.footer).find(D).addClass(s))}),w(n[3]).on("click",function(n){e.change(t).nextMonth()}),w(n[4]).on("click",function(n){e.change(t).nextYear()})}),w.each(e.table,function(t,n){var a=w(n).find("td");a.on("click",function(){e.choose(w(this))})}),w(e.footer).find("span").on("click",function(){var t=w(this).attr("lay-type");e.tool(this,t)})},T.prototype.isInput=function(e){return/input|textarea/.test(e.tagName.toLocaleLowerCase())},T.prototype.events=function(){var e=this,t=e.config,n=function(n,a){n.on(t.trigger,function(){a&&(e.bindElem=this),e.render()})};t.elem[0]&&!t.elem[0].eventHandler&&(n(t.elem,"bind"),n(t.eventElem),w(document).on("click",function(n){n.target!==t.elem[0]&&n.target!==t.eventElem[0]&&n.target!==w(t.closeStop)[0]&&e.remove()}).on("keydown",function(t){13===t.keyCode&&w("#"+e.elemID)[0]&&e.elemID===T.thisElem&&(t.preventDefault(),w(e.footer).find(g)[0].click())}),w(window).on("resize",function(){return!(!e.elem||!w(r)[0])&&void e.position()}),t.elem[0].eventHandler=!0)},n.render=function(e){var t=new T(e);return a.call(t)},n.getEndDate=function(e,t){var n=new Date;return n.setFullYear(t||n.getFullYear(),e||n.getMonth()+1,1),new Date(n.getTime()-864e5).getDate()},window.lay=window.lay||w,e?(n.ready(),layui.define(function(e){n.path=layui.cache.dir,e(i,n)})):"function"==typeof define&&define.amd?define(function(){return n}):function(){n.ready(),window.laydate=n}()}();
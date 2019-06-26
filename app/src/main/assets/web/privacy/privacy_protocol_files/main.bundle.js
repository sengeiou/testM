;/*!src/static/lib/Q_uploader/Q.js*/
/*
* Q.js for Uploader
* author:devin87@qq.com
* update:2017/09/22 14:50
*/
(function (window, undefined) {
    "use strict";

    var toString = Object.prototype.toString,
        has = Object.prototype.hasOwnProperty,
        slice = Array.prototype.slice;

    //若value不为undefine,则返回value;否则返回defValue
    function def(value, defValue) {
        return value !== undefined ? value : defValue;
    }

    //检测是否为函数
    function isFunc(fn) {
        //在ie11兼容模式（ie6-8）下存在bug,当调用次数过多时可能返回不正确的结果
        //return typeof fn == "function";

        return toString.call(fn) === "[object Function]";
    }

    //检测是否为正整数
    function isUInt(n) {
        return typeof n == "number" && n > 0 && n === Math.floor(n);
    }

    //触发指定函数,如果函数不存在,则不触发
    function fire(fn, bind) {
        if (isFunc(fn)) return fn.apply(bind, slice.call(arguments, 2));
    }

    //扩展对象
    //forced:是否强制扩展
    function extend(destination, source, forced) {
        if (!destination || !source) return destination;

        for (var key in source) {
            if (key == undefined || !has.call(source, key)) continue;

            if (forced || destination[key] === undefined) destination[key] = source[key];
        }
        return destination;
    }

    //Object.forEach
    extend(Object, {
        //遍历对象
        forEach: function (obj, fn, bind) {
            for (var key in obj) {
                if (has.call(obj, key)) fn.call(bind, key, obj[key], obj);
            }
        }
    });

    extend(Array.prototype, {
        //遍历对象
        forEach: function (fn, bind) {
            var self = this;
            for (var i = 0, len = self.length; i < len; i++) {
                if (i in self) fn.call(bind, self[i], i, self);
            }
        }
    });

    extend(Date, {
        //获取当前日期和时间所代表的毫秒数
        now: function () {
            return +new Date;
        }
    });

    //-------------------------- browser ---------------------------
    var browser_ie;

    //ie11 开始不再保持向下兼容(例如,不再支持 ActiveXObject、attachEvent 等特性)
    if (window.ActiveXObject || window.msIndexedDB) {
        //window.ActiveXObject => ie10-
        //window.msIndexedDB   => ie11+

        browser_ie = document.documentMode || (!!window.XMLHttpRequest ? 7 : 6);
    }

    //-------------------------- json ---------------------------

    //json解析
    //secure:是否进行安全检测
    function json_decode(text, secure) {
        //安全检测
        if (secure !== false && !/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, "@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, "]").replace(/(?:^|:|,)(?:\s*\[)+/g, ""))) throw new Error("JSON SyntaxError");
        try {
            return (new Function("return " + text))();
        } catch (e) { }
    }

    if (!window.JSON) window.JSON = {};
    if (!JSON.parse) JSON.parse = json_decode;

    //-------------------------- DOM ---------------------------

    //设置元素透明
    function setOpacity(ele, value) {
        if (value <= 1) value *= 100;

        if (ele.style.opacity != undefined) ele.style.opacity = value / 100;
        else if (ele.style.filter != undefined) ele.style.filter = "alpha(opacity=" + parseInt(value) + ")";
    }

    //获取元素绝对定位
    function getOffset(ele, root) {
        var left = 0, top = 0, width = ele.offsetWidth, height = ele.offsetHeight;

        do {
            left += ele.offsetLeft;
            top += ele.offsetTop;
            ele = ele.offsetParent;
        } while (ele && ele != root);

        return { left: left, top: top, width: width, height: height };
    }

    //遍历元素节点
    function walk(ele, walk, start, all) {
        var el = ele[start || walk];
        var list = [];
        while (el) {
            if (el.nodeType == 1) {
                if (!all) return el;
                list.push(el);
            }
            el = el[walk];
        }
        return all ? list : null;
    }

    //获取上一个元素节点
    function getPrev(ele) {
        return ele.previousElementSibling || walk(ele, "previousSibling", null, false);
    }

    //获取下一个元素节点
    function getNext(ele) {
        return ele.nextElementSibling || walk(ele, "nextSibling", null, false);
    }

    //获取第一个元素子节点
    function getFirst(ele) {
        return ele.firstElementChild || walk(ele, "nextSibling", "firstChild", false);
    }

    //获取最后一个元素子节点
    function getLast(ele) {
        return ele.lastElementChild || walk(ele, "previousSibling", "lastChild", false);
    }

    //获取所有子元素节点
    function getChilds(ele) {
        return ele.children || walk(ele, "nextSibling", "firstChild", true);
    }

    //创建元素
    function createEle(tagName, className, html) {
        var ele = document.createElement(tagName);
        if (className) ele.className = className;
        if (html) ele.innerHTML = html;

        return ele;
    }

    //解析html标签
    function parseHTML(html, all) {
        var box = createEle("div", "", html);
        return all ? box.childNodes : getFirst(box);
    }

    //-------------------------- event ---------------------------

    var addEvent,
        removeEvent;

    if (document.addEventListener) {  //w3c
        addEvent = function (ele, type, fn) {
            ele.addEventListener(type, fn, false);
        };

        removeEvent = function (ele, type, fn) {
            ele.removeEventListener(type, fn, false);
        };
    } else if (document.attachEvent) {  //IE
        addEvent = function (ele, type, fn) {
            ele.attachEvent("on" + type, fn);
        };

        removeEvent = function (ele, type, fn) {
            ele.detachEvent("on" + type, fn);
        };
    }

    //event简单处理
    function fix_event(event) {
        var e = event || window.event;

        //for ie
        if (!e.target) e.target = e.srcElement;

        return e;
    }

    //添加事件
    function add_event(element, type, handler, once) {
        var fn = function (e) {
            handler.call(element, fix_event(e));

            if (once) removeEvent(element, type, fn);
        };

        addEvent(element, type, fn);

        if (!once) {
            return {
                //直接返回停止句柄 eg:var api=add_event();api.stop();
                stop: function () {
                    removeEvent(element, type, fn);
                }
            };
        }
    }

    //触发事件
    function trigger_event(ele, type) {
        if (isFunc(ele[type])) ele[type]();
        else if (ele.fireEvent) ele.fireEvent("on" + type);  //ie10-
        else if (ele.dispatchEvent) {
            var evt = document.createEvent("HTMLEvents");

            //initEvent接受3个参数:事件类型,是否冒泡,是否阻止浏览器的默认行为
            evt.initEvent(type, true, true);

            //鼠标事件,设置更多参数
            //var evt = document.createEvent("MouseEvents");
            //evt.initMouseEvent(type, true, true, ele.ownerDocument.defaultView, 1, e.screenX, e.screenY, e.clientX, e.clientY, false, false, false, false, 0, null);

            ele.dispatchEvent(evt);
        }
    }

    //阻止事件默认行为并停止事件冒泡
    function stop_event(event, isPreventDefault, isStopPropagation) {
        var e = fix_event(event);

        //阻止事件默认行为
        if (isPreventDefault !== false) {
            if (e.preventDefault) e.preventDefault();
            else e.returnValue = false;
        }

        //停止事件冒泡
        if (isStopPropagation !== false) {
            if (e.stopPropagation) e.stopPropagation();
            else e.cancelBubble = true;
        }
    }

    //---------------------- other ----------------------

    var RE_HTTP = /^https?:\/\//i;

    //是否http路径(以 http:// 或 https:// 开头)
    function isHttpURL(url) {
        return RE_HTTP.test(url);
    }

    //判断指定路径与当前页面是否同域(包括协议检测 eg:http与https不同域)
    function isSameHost(url) {
        if (!isHttpURL(url)) return true;

        var start = RegExp.lastMatch.length,
            end = url.indexOf("/", start),
            host = url.slice(0, end != -1 ? end : undefined);

        return host.toLowerCase() == (location.protocol + "//" + location.host).toLowerCase();
    }

    //按照进制解析数字的层级 eg:时间转化 -> parseLevel(86400,[60,60,24]) => { value=1, level=3 }
    //steps:步进,可以是固定的数字(eg:1024),也可以是具有层次关系的数组(eg:[60,60,24])
    //limit:限制解析的层级,正整数,默认为100
    function parseLevel(size, steps, limit) {
        size = +size;
        steps = steps || 1024;

        var level = 0,
            isNum = typeof steps == "number",
            stepNow = 1,
            count = isUInt(limit) ? limit : (isNum ? 100 : steps.length);

        while (size >= stepNow && level < count) {
            stepNow *= (isNum ? steps : steps[level]);
            level++;
        }

        if (level && size < stepNow) {
            stepNow /= (isNum ? steps : steps.last());
            level--;
        }

        return { value: level ? size / stepNow : size, level: level };
    }

    var UNITS_FILE_SIZE = ["B", "KB", "MB", "GB", "TB", "PB", "EB"];

    //格式化数字输出,将数字转为合适的单位输出,默认按照1024层级转为文件单位输出
    function formatSize(size, ops) {
        ops = ops === true ? { all: true } : ops || {};

        if (isNaN(size) || size == undefined || size < 0) {
            var error = ops.error || "--";

            return ops.all ? { text: error } : error;
        }

        var pl = parseLevel(size, ops.steps, ops.limit),

            value = pl.value,
            text = value.toFixed(def(ops.digit, 2));

        if (ops.trim !== false && text.lastIndexOf(".") != -1) text = text.replace(/\.?0+$/, "");

        pl.text = text + (ops.join || "") + (ops.units || UNITS_FILE_SIZE)[pl.level + (ops.start || 0)];

        return ops.all ? pl : pl.text;
    }

    //---------------------- export ----------------------

    var Q = {
        def: def,
        isFunc: isFunc,
        isUInt: isUInt,

        fire: fire,
        extend: extend,

        ie: browser_ie,

        setOpacity: setOpacity,
        getOffset: getOffset,

        walk: walk,
        getPrev: getPrev,
        getNext: getNext,
        getFirst: getFirst,
        getLast: getLast,
        getChilds: getChilds,

        createEle: createEle,
        parseHTML: parseHTML,

        isHttpURL: isHttpURL,
        isSameHost: isSameHost,

        parseLevel: parseLevel,
        formatSize: formatSize
    };

    if (browser_ie) Q["ie" + (browser_ie < 6 ? 6 : browser_ie)] = true;

    Q.event = {
        fix: fix_event,
        stop: stop_event,
        trigger: trigger_event,

        add: add_event
    };

    window.Q = Q;

})(window);
;/*!src/static/lib/Q_uploader/Q.Uploader.js*/
/// <reference path="Q.js" />
/// <reference path="Q.md5File.js" />
/*
 * Q.Uploader.js 文件上传管理器 1.0
 * https://github.com/devin87/web-uploader
 * author:devin87@qq.com
 * update:2018/11/07 12:27
 */
(function (window, undefined) {
  "use strict";

  var def = Q.def,
    fire = Q.fire,
    extend = Q.extend,

    getFirst = Q.getFirst,
    getLast = Q.getLast,

    parseJSON = JSON.parse,

    createEle = Q.createEle,
    parseHTML = Q.parseHTML,

    setOpacity = Q.setOpacity,
    getOffset = Q.getOffset,

    md5File = Q.md5File,

    E = Q.event,
    addEvent = E.add,
    triggerEvent = E.trigger,
    stopEvent = E.stop;

  //Object.forEach
  //Date.now

  //-------------------------------- Uploader --------------------------------

  var support_html5_upload = false, //是否支持html5(ajax)方式上传
    support_multiple_select = false, //是否支持文件多选

    support_file_click_trigger = false, //上传控件是否支持click触发文件选择 eg: input.click() => ie9及以下不支持

    UPLOADER_GUID = 0, //文件上传管理器唯一标示,多用于同一个页面存在多个管理器的情况

    UPLOAD_TASK_GUID = 0, //上传任务唯一标示
    UPLOAD_HTML4_ZINDEX = 0; //防止多个上传管理器的触发按钮位置重复引起的问题

  //上传状态
  var UPLOAD_STATE_READY = 0, //任务已添加,准备上传
    UPLOAD_STATE_PROCESSING = 1, //任务上传中
    UPLOAD_STATE_COMPLETE = 2, //任务上传完成

    UPLOAD_STATE_SKIP = -1, //任务已跳过(不会上传)
    UPLOAD_STATE_CANCEL = -2, //任务已取消
    UPLOAD_STATE_ERROR = -3; //任务已失败

  var global_settings = {};

  //Uploader全局设置
  function setup(ops) {
    extend(global_settings, ops, true);
  }

  //获取上传状态说明
  function get_upload_status_text(state) {
    var LANG = Uploader.Lang;

    switch (state) {
      case UPLOAD_STATE_READY:
        return LANG.status_ready;
      case UPLOAD_STATE_PROCESSING:
        return LANG.status_processing;
      case UPLOAD_STATE_COMPLETE:
        return LANG.status_complete;

      case UPLOAD_STATE_SKIP:
        return LANG.status_skip;
      case UPLOAD_STATE_CANCEL:
        return LANG.status_cancel;
      case UPLOAD_STATE_ERROR:
        return LANG.status_error;
    }

    return state;
  }

  //上传探测
  function detect() {
    var XHR = window.XMLHttpRequest;
    if (XHR && new XHR().upload && window.FormData) support_html5_upload = true;

    var input = document.createElement("input");
    input.type = "file";

    support_multiple_select = !!input.files;
    support_file_click_trigger = support_html5_upload;
  }

  //截取字符串
  function get_last_find(str, find) {
    var index = str.lastIndexOf(find);
    return index != -1 ? str.slice(index) : "";
  }

  //将逗号分隔的字符串转为键值对
  function split_to_map(str) {
    if (!str) return;

    var list = str.split(","),
      map = {};

    for (var i = 0, len = list.length; i < len; i++) {
      map[list[i]] = true;
    }

    return map;
  }

  //iframe load 事件
  //注意：低版本 ie 支持 iframe 的 onload 事件,不过是隐形的(iframe.onload 方式绑定的将不会触发),需要通过 attachEvent 来注册
  function bind_iframe_load(iframe, fn) {
    if (iframe.attachEvent) iframe.attachEvent("onload", fn);
    else iframe.addEventListener("load", fn, false);
  }

  //计算上传速度
  function set_task_speed(task, total, loaded) {
    if (!total || total <= 0) return;

    var nowTime = Date.now(),
      tick;

    //上传完毕,计算平均速度(Byte/s)
    if (loaded >= total) {
      tick = nowTime - task.startTime;
      if (tick) task.avgSpeed = Math.min(Math.round(total * 1000 / tick), total);
      else if (!task.speed) task.avgSpeed = task.speed = total;

      task.time = tick || 0;
      task.endTime = nowTime;
      return;
    }

    //即时速度(Byte/s)
    tick = nowTime - task.lastTime;
    if (tick < 200) return;

    task.speed = Math.min(Math.round((loaded - task.loaded) * 1000 / tick), task.total);
    task.lastTime = nowTime;
  }

  /*
      文件上传管理器,调用示例
      new Uploader({
          //--------------- 必填 ---------------
          url: "",            //上传路径
          target: element,    //上传按钮，可为数组
          view: element,      //上传任务视图(需加载UI接口默认实现)

          //--------------- 可选 ---------------
          html5: true,       //是否启用html5上传,若浏览器不支持,则自动禁用
          multiple: true,    //选择文件时是否允许多选,若浏览器不支持,则自动禁用(仅html5模式有效)

          clickTrigger:true, //是否启用click触发文件选择 eg: input.click() => ie9及以下不支持

          auto: true,        //添加任务后是否立即上传

          data: {},          //上传文件的同时可以指定其它参数,该参数将以POST的方式提交到服务器

          workerThread: 1,   //同时允许上传的任务数(仅html5模式有效)

          upName: "upfile",  //上传参数名称,若后台需要根据name来获取上传数据,可配置此项
          accept: "",        //指定浏览器接受的文件类型 eg:image/*,video/*
          isDir: false,      //是否是文件夹上传（仅Webkit内核浏览器和新版火狐有效）

          allows: "",        //允许上传的文件类型(扩展名),多个之间用逗号隔开
          disallows: "",     //禁止上传的文件类型(扩展名)

          maxSize: 2*1024*1024,   //允许上传的最大文件大小,字节,为0表示不限(仅对支持的浏览器生效,eg: IE10+、Firefox、Chrome)

          isSlice: false,               //是否启用分片上传，若为true，则isQueryState和isMd5默认为true
          chunkSize: 2 * 1024 * 1024,   //默认分片大小为2MB
          isQueryState:false,           //是否查询文件状态（for 秒传或续传）
          isMd5: false,                 //是否计算上传文件md5值
          isUploadAfterHash:true,       //是否在Hash计算完毕后再上传
          sliceRetryCount:2,            //分片上传失败重试次数

          container:element, //一般无需指定
          getPos:function,   //一般无需指定

          //上传回调事件(function)
          on: {
              init,          //上传管理器初始化完毕后触发

              select,        //点击上传按钮准备选择上传文件之前触发,返回false可禁止选择文件
              add[Async],    //添加任务之前触发,返回false将跳过该任务
              upload[Async], //上传任务之前触发,返回false将跳过该任务
              send[Async],   //发送数据之前触发,返回false将跳过该任务

              cancel,        //取消上传任务后触发
              remove,        //移除上传任务后触发

              progress,      //上传进度发生变化后触发(仅html5模式有效)
              complete       //上传完成后触发
          },

          //UI接口(function),若指定了以下方法,将忽略默认实现
          UI:{
              init,       //执行初始化操作
              draw,       //添加任务后绘制任务界面
              update,     //更新任务界面
              over        //任务上传完成
          }
      });
  */
  function Uploader(settings) {
    var self = this,
      ops = settings || {};

    self.guid = ops.guid || "uploader" + (++UPLOADER_GUID);

    self.list = [];
    self.map = {};

    self.index = 0;
    self.started = false;

    self.set(ops)._init();
  }

  Uploader.prototype = {
    //修复constructor指向
    constructor: Uploader,

    set: function (settings) {
      var self = this,
        ops = extend(settings, self.ops);

      self.url = ops.url; //上传路径
      self.dataType = ops.dataType || "json"; //返回值类型
      self.data = ops.data; //上传参数

      //上传按钮
      self.targets = ops.target || [];
      if (!self.targets.forEach) self.targets = [self.targets];

      self.target = self.targets[0]; //当前上传按钮

      //是否以html5(ajax)方式上传
      self.html5 = support_html5_upload && !!def(ops.html5, true);

      //是否允许多选(仅在启用了html5的情形下生效)
      //在html4模式下,input是一个整体,若启用多选,将无法针对单一的文件进行操作(eg:根据扩展名筛选、取消、删除操作等)
      //若无需对文件进行操作,可通过 uploader.multiple = true 强制启用多选(不推荐)
      self.multiple = support_multiple_select && self.html5 && !!def(ops.multiple, true);

      //是否启用click触发文件选择 eg: input.click() => IE9及以下不支持
      self.clickTrigger = support_file_click_trigger && !!def(ops.clickTrigger, true);

      //允许同时上传的数量(html5有效)
      //由于设计原因,html4仅能同时上传1个任务,请不要更改
      self.workerThread = self.html5 ? ops.workerThread || 1 : 1;

      //空闲的线程数量
      self.workerIdle = self.workerThread;

      //是否在添加任务后自动开始
      self.auto = ops.auto !== false;

      //input元素的name属性
      self.upName = ops.upName || "upfile";

      //input元素的accept属性,用来指定浏览器接受的文件类型 eg:image/*,video/*
      //注意：IE9及以下不支持accept属性
      self.accept = ops.accept || ops.allows;

      //是否是文件夹上传，仅Webkit内核浏览器和新版火狐有效
      self.isDir = ops.isDir;

      //允许上传的文件类型（扩展名）,多个之间用逗号隔开 eg:.jpg,.png
      self.allows = split_to_map(ops.allows);

      //禁止上传的文件类型（扩展名）,多个之间用逗号隔开
      self.disallows = split_to_map(ops.disallows);

      //允许上传的最大文件大小,字节,为0表示不限(仅对支持的浏览器生效,eg: IE10+、Firefox、Chrome)
      self.maxSize = +ops.maxSize || 0;

      self.isSlice = !!ops.isSlice; //是否启用分片上传
      self.chunkSize = +ops.chunkSize || 2 * 1024 * 1024; //分片上传大小
      self.isQueryState = !!def(ops.isQueryState, self.isSlice); //是否查询文件状态（for 秒传或续传）
      self.isMd5 = !!def(ops.isMd5, self.isSlice); //是否计算上传文件md5值
      self.isUploadAfterHash = ops.isUploadAfterHash !== false; //是否在Hash计算完毕后再上传
      self.sliceRetryCount = ops.sliceRetryCount == undefined ? 2 : +ops.sliceRetryCount || 0; //分片上传失败重试次数

      //ie9及以下不支持click触发(即使能弹出文件选择框,也无法获取文件数据,报拒绝访问错误)
      //若上传按钮位置不确定(比如在滚动区域内),则无法触发文件选择
      //设置原则:getPos需返回上传按钮距container的坐标
      self.container = ops.container || document.body;

      //函数,获取上传按钮距container的坐标,返回格式 eg:{ left: 100, top: 100 }
      if (ops.getPos) self.getPos = ops.getPos;

      //UI接口,此处将覆盖 prototype 实现
      var UI = ops.UI || {};
      if (UI.init) self.init = UI.init; //执行初始化操作
      if (UI.draw) self.draw = UI.draw; //添加任务后绘制任务界面
      if (UI.update) self.update = UI.update; //更新任务界面
      if (UI.over) self.over = UI.over; //任务上传完成

      //上传回调事件
      self.fns = ops.on || {};

      //上传选项
      self.ops = ops;

      if (self.accept && !self.clickTrigger) self.resetInput();

      return self;
    },

    //初始化上传管理器
    _init: function () {
      var self = this;

      if (self._inited) return;
      self._inited = true;

      var guid = self.guid,
        container = self.container;

      var boxInput = createEle("div", "upload-input " + guid);
      container.appendChild(boxInput);
      // <object id="file-object"></object>

      self.boxInput = boxInput;

      //构造html4上传所需的iframe和form
      if (!self.html5) {
        var iframe_name = "upload_iframe_" + guid;
        var html = '<iframe class="u-iframe" name="' + iframe_name + '"></iframe>' +
          '<form class="u-form" action="" method="post" enctype="multipart/form-data" target="' + iframe_name + '"></form>';

        var boxHtml4 = createEle("div", "upload-html4 " + guid, html);
        document.body.appendChild(boxHtml4);

        var iframe = getFirst(boxHtml4),
          form = getLast(boxHtml4);

        self.iframe = iframe;
        self.form = form;

        //html4上传完成回调
        bind_iframe_load(iframe, function () {
          // if (self.workerIdle != 0) return;
          if (self.workerIdle >= 2) return;

          var text;
          try {
            text = iframe.contentWindow.document.body.innerHTML;
          } catch (e) {}

          self.complete(undefined, UPLOAD_STATE_COMPLETE, text);
        });
      }

      self.targets.forEach(function (target) {
        if (self.clickTrigger) {
          addEvent(target, "click", function (e) {
            if (self.fire("select", e) === false) return;

            self.resetInput();

            //注意:ie9及以下可以弹出文件选择框,但获取不到选择数据,拒绝访问。
            triggerEvent(self.inputFile, "click");
          });
        } else {
          addEvent(target, "mouseover", function (e) {
            self.target = this;
            self.updatePos();
          });
        }
      });

      //html4点击事件
      if (!self.clickTrigger) {
        addEvent(boxInput, "click", function (e) {
          if (self.fire("select", e) === false) stopEvent(e);
        });

        setOpacity(boxInput, 0);

        self.resetInput();
      }

      self.fire("init");

      return self.run("init");
    },

    //重置上传控件
    resetInput: function () {
      var self = this,
        boxInput = self.boxInput;

      if (!boxInput) return self;
      boxInput.innerHTML = '<input type="file" name="' + self.upName + '"' + (self.accept ? 'accept="' + self.accept + '"' : '') + (self.isDir ? 'webkitdirectory=""' : '') + ' style="' + (self.clickTrigger ? 'visibility: hidden;' : 'font-size:100px;') + '"' + (self.multiple ? ' multiple="multiple"' : '') + '>';
      var inputFile = getFirst(boxInput);

      //文件选择事件
      addEvent(inputFile, "change", function (e) {
        self.add(this);

        //html4 重置上传控件
        if (!self.html5) self.resetInput();
      });

      self.inputFile = inputFile;

      return self.updatePos();

    },
    //更新上传按钮坐标(for ie)
    updatePos: function (has_more_uploader) {
      var self = this;
      if (self.clickTrigger) return self;

      var getPos = self.getPos || getOffset,

        boxInput = self.boxInput,
        inputFile = getFirst(boxInput),
        target = self.target,

        inputWidth = target.offsetWidth,
        inputHeight = target.offsetHeight;

      // pos = inputWidth == 0 ? { left: -10000, top: -10000 } : getPos(target);

      boxInput.style.width = inputFile.style.width = inputWidth + "px";
      boxInput.style.height = inputFile.style.height = inputHeight + "px";
      if($(boxInput).parent('body').length){
        boxInput.style.left = $(target).offset().left + "px";
        boxInput.style.top = $(target).offset().top + "px";
      }else{
        boxInput.style.left = target.offsetLeft + "px";
        boxInput.style.top = target.offsetTop + "px";
      }

      //多用于选项卡切换中上传按钮位置重复的情况
      if (has_more_uploader) boxInput.style.zIndex = ++UPLOAD_HTML4_ZINDEX;

      return self;
    },
    //触发ops上定义的回调方法,优先触发异步回调(以Async结尾)
    fire: function (action, arg, callback) {
      if (!callback) return fire(this.fns[action], this, arg);

      var asyncFun = this.fns[action + "Async"];
      if (asyncFun) return fire(asyncFun, this, arg, callback);

      callback(fire(this.fns[action], this, arg));
    },

    //运行内部方法或扩展方法(如果存在)
    run: function (action, arg) {
      var fn = this[action];
      if (fn) fire(fn, this, arg);
      return this;
    },
    
    //添加一个上传任务
    addTask: function (input, file) {
      if (!input && !file) return;
      // debugger;

      var name, size;
      var test = document.querySelector('.videos-test.hide');

      if (!test) {
        test = document.body.appendChild(createEle('div', 'videos-test hide'));
      }

      if (file) {
        name = file.webkitRelativePath || file.name || file.fileName;
        size = file.size === 0 ? 0 : file.size || file.fileSize;
        
      } else {
        name = get_last_find(input.value, "\\").slice(1) || input.value;
        size = -1;
      }
      
      var self = this,
      ext = get_last_find(name, ".").toLowerCase(),
      limit_type;
      
      if ((self.disallows && self.disallows[ext]) || (self.allows && !self.allows[ext])) limit_type = "ext";
      else if (size != -1 && self.maxSize && size > self.maxSize) limit_type = "size";
      
      var task = {
        id: ++UPLOAD_TASK_GUID,
        
        name: name,
        ext: ext,
        size: size,
        
        input: input,
        file: file,
        
        state: limit_type ? UPLOAD_STATE_SKIP : UPLOAD_STATE_READY
      };

      if (limit_type) {
        task.limited = limit_type;
        task.disabled = true;
      }
      
      // 获取视频时长，这个是异步，所以 fireFn要在异步回调中执行
      // this.ops.videosAccept 是外部传过来的参数，是视频文件的尾缀
      if (this.ops.videosAccept && this.ops.videosAccept.indexOf(task.ext.toLocaleLowerCase()) >= 0) {
        var src = '';
        if (file) {
          // file对象
          src = URL.createObjectURL(file);
          var video = test.appendChild(parseHTML('<video src="'+ src +'" controls="controls">'));
          video.addEventListener('canplaythrough', function () {
            var hour = parseInt(this.duration / 3600);
            var minute = parseInt((this.duration % 3600) / 60);
            task.videoHour = hour;
            task.videoMinute = minute;
            // ~~ 等同于 Math.floor()
            var second = ~~(this.duration % 60);
            task.videoSecond = second;
            fireFn(task);
            test.removeChild(this);
          });
        }else {
          fireFn(task);
        }
      }else {
        // 图片
        fireFn(task);
      }

      function fireFn(task) {
        self.fire("add", task, function (result) {
          if (result === false || task.disabled || task.limited) return;
  
          task.index = self.list.length;
          self.list.push(task);
          self.map[task.id] = task;
  
          self.run("draw", task);
  
          if (self.auto) self.start();
        });
      }
      
      return task;
    },

    //添加上传任务,自动判断input(是否多选)或file
    add: function (input_or_file) {
      var self = this;

      if (input_or_file.tagName == "INPUT") {
        var files = input_or_file.files;
        if (files) {
          for (var i = 0, len = files.length; i < len; i++) {
            self.addTask(input_or_file, files[i]);
          }
        } else {
          self.addTask(input_or_file);
        }
      } else {
        self.addTask(undefined, input_or_file);
      }
    },

    //批量添加上传任务
    addList: function (list) {
      for (var i = 0, len = list.length; i < len; i++) {
        this.add(list[i]);
      }
    },

    //获取指定任务
    get: function (taskId) {
      if (taskId != undefined) return this.map[taskId];
    },

    //取消上传任务
    //onlyCancel: 若为true,则仅取消上传而不触发任务完成事件
    cancel: function (taskId, onlyCancel) {
      var self = this,
        task = self.get(taskId);

      if (!task) return;

      var state = task.state;

      //若任务已完成,直接返回
      if (state != UPLOAD_STATE_READY && state != UPLOAD_STATE_PROCESSING) return self;

      if (state == UPLOAD_STATE_PROCESSING) {
        //html5
        var xhr = task.xhr;
        if (xhr) {
          xhr.abort();

          //无需调用complete,html5 有自己的处理,此处直接返回
          return self;
        }

        //html4
        self.iframe.contentWindow.location = "about:blank";
      }

      return onlyCancel ? self : self.complete(task, UPLOAD_STATE_CANCEL);
    },

    //移除任务
    remove: function (taskId) {
      var task = this.get(taskId);
      if (!task) return;

      if (task.state == UPLOAD_STATE_PROCESSING) this.cancel(taskId);

      //this.list.splice(task.index, 1);
      //this.map[task.id] = undefined;

      //从数组中移除任务时,由于任务是根据index获取,若不处理index,将导致上传错乱甚至不能上传
      //此处重置上传索引,上传时会自动修正为正确的索引(程序会跳过已处理过的任务)
      //this.index = 0;

      //添加移除标记(用户可以自行操作,更灵活)
      task.deleted = true;

      this.fire("remove", task);
    },

    //开始上传
    start: function () {
      var self = this,

        workerIdle = self.workerIdle,

        list = self.list,
        index = self.index,

        count = list.length;

      if (!self.started) {
        self.started = true;
      }

      if (count <= 0 || index >= count || workerIdle <= 0) return self;

      var task = list[index];
      self.index++;

      return self.upload(task);
    },

    //上传任务
    upload: function (task) {
      var self = this;

      if (!task || task.state != UPLOAD_STATE_READY || task.skip || task.deleted) return self.start();

      task.url = self.url;
      self.workerIdle--;

      self.fire("upload", task, function (result) {
        if (result === false) return self.complete(task, UPLOAD_STATE_SKIP);

        if (self.html5 && task.file) self._upload_html5_ready(task);
        else if (task.input) self._upload_html4(task);
        else self.complete(task, UPLOAD_STATE_SKIP);
      });

      return self;
    },

    _process_xhr_headers: function (xhr) {
      var ops = this.ops;

      //设置http头(必须在 xhr.open 之后)
      var fn = function (k, v) {
        xhr.setRequestHeader(k, v);
      };

      if (global_settings.headers) Object.forEach(global_settings.headers, fn);
      if (ops.headers) Object.forEach(ops.headers, fn);
    },

    //根据 task.hash 查询任务状态（for 秒传或续传）
    queryState: function (task, callback) {
      var self = this,
        url = self.url,
        xhr = new XMLHttpRequest();

      var params = ["action=query", "hash=" + (task.hash || encodeURIComponent(task.name)), "fileName=" + encodeURIComponent(task.name)];
      if (task.size != -1) params.push("fileSize=" + task.size);

      self._process_params(task, function (k, v) {
        params.push(encodeURIComponent(k) + "=" + (v != undefined ? encodeURIComponent(v) : ""));
      }, "dataQuery");

      task.queryUrl = url + (url.indexOf("?") == -1 ? "?" : "&") + params.join("&");

      //秒传查询事件
      self.fire("sliceQuery", task);

      xhr.open("GET", task.queryUrl);
      self._process_xhr_headers(xhr);

      xhr.onreadystatechange = function () {
        if (xhr.readyState != 4) return;

        var responseText, json;

        if (xhr.status >= 200 && xhr.status < 400) {
          responseText = xhr.responseText;

          if (responseText === "ok") json = {
            ret: 1
          };
          else if (responseText) json = parseJSON(responseText);

          if (!json || typeof json == "number") json = {
            ret: 0,
            start: json
          };

          task.response = responseText;
          task.json = json;

          if (json.ret == 1) {
            task.queryOK = true;
            self.cancel(task.id, true).complete(task, UPLOAD_STATE_COMPLETE);
          } else {
            var start = +json.start || 0;
            if (start != Math.floor(start)) start = 0;

            task.sliceStart = start;
          }
        }

        fire(callback, self, xhr);
      };

      xhr.onerror = function () {
        fire(callback, self, xhr);
      };

      xhr.send(null);

      return self;
    },

    //处理html5上传（包括秒传和断点续传）
    _upload_html5_ready: function (task) {
      var self = this;

      //上传处理
      var goto_upload = function () {
        if (task.state == UPLOAD_STATE_COMPLETE) return;

        if (self.isSlice) self._upload_slice(task);
        else self._upload_html5(task);
      };

      var after_hash = function (callback) {
        //自定义hash事件
        self.fire("hash", task, function () {
          if (task.hash && self.isQueryState && task.state != UPLOAD_STATE_COMPLETE) self.queryState(task, callback);
          else callback();
        });
      };

      //计算文件hash
      var compute_hash = function (callback) {
        //计算上传文件md5值
        if (self.isMd5 && md5File) {
          var hashProgress = self.fns.hashProgress;

          md5File(task.file, function (md5, time) {
            task.hash = md5;
            task.timeHash = time;
            after_hash(callback);
          }, function (pvg) {
            fire(hashProgress, self, task, pvg);
          });
        } else {
          after_hash(callback);
        }
      };

      if (self.isUploadAfterHash) {
        compute_hash(goto_upload);
      } else {
        goto_upload();
        compute_hash();
      }

      return self;
    },

    //处理上传参数
    _process_params: function (task, fn, prop) {
      prop = prop || "data";
      if (global_settings.data) Object.forEach(global_settings.data, fn);
      if (this.data) Object.forEach(this.data, fn);
      if (task && task[prop]) Object.forEach(task[prop], fn);
    },

    //以html5的方式上传任务
    _upload_html5: function (task) {
      var self = this,
        xhr = new XMLHttpRequest();

      task.xhr = xhr;

      xhr.upload.addEventListener("progress", function (e) {
        self.progress(task, e.total, e.loaded);
      }, false);

      xhr.addEventListener("load", function (e) {
        self.complete(task, UPLOAD_STATE_COMPLETE, e.target.responseText);
      }, false);

      xhr.addEventListener("error", function () {
        self.complete(task, UPLOAD_STATE_ERROR);
      }, false);

      xhr.addEventListener("abort", function () {
        self.complete(task, UPLOAD_STATE_CANCEL);
      }, false);

      var fd = new FormData();

      //处理上传参数
      fd.append('name', task.name)
      self._process_params(task, function (k, v) {
        v !== undefined && fd.append(k, v);
      });

      fd.append(self.upName, task.blob || task.file, task.name);
      // fd.append("fileName", task.name);

      xhr.open("POST", task.url);
      self._process_xhr_headers(xhr);

      //移除自定义标头,以防止跨域上传被拦截
      //xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");

      self.fire("send", task, function (result) {
        if (result === false) return self.complete(task, UPLOAD_STATE_SKIP);

        xhr.send(fd);

        self._afterSend(task);
      });
    },

    //以传统方式上传任务
    _upload_html4: function (task) {
      var self = this,
        form = self.form,
        input = task.input;

      //解决多选的情况下重复上传的问题(即使如此，仍然不建议html4模式下开启多选)
      if (input._uploaded) return self.complete(task, UPLOAD_STATE_COMPLETE);
      input._uploaded = true;

      input.name = self.upName;

      form.innerHTML = "";
      
      form.action = task.url;
      
      //处理上传参数
      self._process_params(task, function (k, v) {
        v !== undefined && form.appendChild(parseHTML('<input type="hidden" name="' + k + '" value="' + v + '">'));
      });

      // fd.append('name', task.name)
      form.appendChild(parseHTML('<input type="hidden" name="name" value="' + task.name + '">'));
      form.appendChild(input);

      self.fire("send", task, function (result) {
        if (result === false) return self.complete(task, UPLOAD_STATE_SKIP);

        form.submit();

        self._afterSend(task);
      });
    },

    //已开始发送数据
    _afterSend: function (task) {
      task.lastTime = task.startTime = Date.now();

      task.state = UPLOAD_STATE_PROCESSING;
      this._lastTask = task;

      this.progress(task);
    },

    //更新进度显示
    progress: function (task, total, loaded) {
      if (!total) total = task.size;
      if (!loaded || loaded < 0) loaded = 0;

      var state = task.state || UPLOAD_STATE_READY;

      if (loaded > total) loaded = total;
      if (loaded > 0 && state == UPLOAD_STATE_READY) task.state = state = UPLOAD_STATE_PROCESSING;

      var completed = state == UPLOAD_STATE_COMPLETE;
      if (completed) total = loaded = task.size;

      //计算上传速度
      set_task_speed(task, total, loaded);

      task.total = total;
      task.loaded = loaded;

      this.fire("progress", task);
      this.run("update", task);
    },

    //处理响应数据
    _process_response: function (task, responseText) {
      task.response = responseText;
      if (!responseText) return;

      if (this.dataType == "json") task.json = parseJSON(responseText);
    },

    //完成上传
    complete: function (task, state, responseText) {
      var self = this;

      if (!task && self.workerThread == 1) task = self._lastTask;

      if (task) {
        if (state != undefined) task.state = state;

        if (task.state == UPLOAD_STATE_PROCESSING || state == UPLOAD_STATE_COMPLETE) {
          task.state = UPLOAD_STATE_COMPLETE;
          self.progress(task, task.size, task.size);
        }

        if (responseText !== undefined) self._process_response(task, responseText);
      }

      self.run("update", task).run("over", task);

      if (state == UPLOAD_STATE_CANCEL) self.fire("cancel", task);
      self.fire("complete", task);

      self.workerIdle++;
      if (self.started) self.start();

      return self;
    }
  };

  //扩展上传管理器
  //forced:是否强制覆盖
  Uploader.extend = function (source, forced) {
    extend(Uploader.prototype, source, forced);
  };

  //---------------------- export ----------------------
  detect();

  extend(Uploader, {
    support: {
      html5: support_html5_upload,
      multiple: support_multiple_select
    },

    READY: UPLOAD_STATE_READY,
    PROCESSING: UPLOAD_STATE_PROCESSING,
    COMPLETE: UPLOAD_STATE_COMPLETE,

    SKIP: UPLOAD_STATE_SKIP,
    CANCEL: UPLOAD_STATE_CANCEL,
    ERROR: UPLOAD_STATE_ERROR,

    //UI对象,用于多套UI共存
    UI: {},

    //默认语言
    Lang: {
      status_ready: "准备中",
      status_processing: "上传中",
      status_complete: "已完成",
      status_skip: "已跳过",
      status_cancel: "已取消",
      status_error: "已失败"
    },

    setup: setup,
    getStatusText: get_upload_status_text
  });

  Q.Uploader = Uploader;

})(window);

;/*!src/static/js/main.js*/
(function () {
'use strict';

function unwrapExports (x) {
	return x && x.__esModule && Object.prototype.hasOwnProperty.call(x, 'default') ? x['default'] : x;
}

function createCommonjsModule(fn, module) {
	return module = { exports: {} }, fn(module, module.exports), module.exports;
}

var classCallCheck = createCommonjsModule(function (module, exports) {
exports.__esModule = true;

exports.default = function (instance, Constructor) {
  if (!(instance instanceof Constructor)) {
    throw new TypeError("Cannot call a class as a function");
  }
};
});

var _classCallCheck = unwrapExports(classCallCheck);

var _global = createCommonjsModule(function (module) {
// https://github.com/zloirock/core-js/issues/86#issuecomment-115759028
var global = module.exports = typeof window != 'undefined' && window.Math == Math
  ? window : typeof self != 'undefined' && self.Math == Math ? self
  // eslint-disable-next-line no-new-func
  : Function('return this')();
if (typeof __g == 'number') __g = global; // eslint-disable-line no-undef
});

var _core = createCommonjsModule(function (module) {
var core = module.exports = { version: '2.6.1' };
if (typeof __e == 'number') __e = core; // eslint-disable-line no-undef
});

var _core_1 = _core.version;

var _aFunction = function (it) {
  if (typeof it != 'function') throw TypeError(it + ' is not a function!');
  return it;
};

// optional / simple context binding

var _ctx = function (fn, that, length) {
  _aFunction(fn);
  if (that === undefined) return fn;
  switch (length) {
    case 1: return function (a) {
      return fn.call(that, a);
    };
    case 2: return function (a, b) {
      return fn.call(that, a, b);
    };
    case 3: return function (a, b, c) {
      return fn.call(that, a, b, c);
    };
  }
  return function (/* ...args */) {
    return fn.apply(that, arguments);
  };
};

var _isObject = function (it) {
  return typeof it === 'object' ? it !== null : typeof it === 'function';
};

var _anObject = function (it) {
  if (!_isObject(it)) throw TypeError(it + ' is not an object!');
  return it;
};

var _fails = function (exec) {
  try {
    return !!exec();
  } catch (e) {
    return true;
  }
};

// Thank's IE8 for his funny defineProperty
var _descriptors = !_fails(function () {
  return Object.defineProperty({}, 'a', { get: function () { return 7; } }).a != 7;
});

var document = _global.document;
// typeof document.createElement is 'object' in old IE
var is = _isObject(document) && _isObject(document.createElement);
var _domCreate = function (it) {
  return is ? document.createElement(it) : {};
};

var _ie8DomDefine = !_descriptors && !_fails(function () {
  return Object.defineProperty(_domCreate('div'), 'a', { get: function () { return 7; } }).a != 7;
});

// 7.1.1 ToPrimitive(input [, PreferredType])

// instead of the ES6 spec version, we didn't implement @@toPrimitive case
// and the second argument - flag - preferred type is a string
var _toPrimitive = function (it, S) {
  if (!_isObject(it)) return it;
  var fn, val;
  if (S && typeof (fn = it.toString) == 'function' && !_isObject(val = fn.call(it))) return val;
  if (typeof (fn = it.valueOf) == 'function' && !_isObject(val = fn.call(it))) return val;
  if (!S && typeof (fn = it.toString) == 'function' && !_isObject(val = fn.call(it))) return val;
  throw TypeError("Can't convert object to primitive value");
};

var dP = Object.defineProperty;

var f = _descriptors ? Object.defineProperty : function defineProperty(O, P, Attributes) {
  _anObject(O);
  P = _toPrimitive(P, true);
  _anObject(Attributes);
  if (_ie8DomDefine) try {
    return dP(O, P, Attributes);
  } catch (e) { /* empty */ }
  if ('get' in Attributes || 'set' in Attributes) throw TypeError('Accessors not supported!');
  if ('value' in Attributes) O[P] = Attributes.value;
  return O;
};

var _objectDp = {
	f: f
};

var _propertyDesc = function (bitmap, value) {
  return {
    enumerable: !(bitmap & 1),
    configurable: !(bitmap & 2),
    writable: !(bitmap & 4),
    value: value
  };
};

var _hide = _descriptors ? function (object, key, value) {
  return _objectDp.f(object, key, _propertyDesc(1, value));
} : function (object, key, value) {
  object[key] = value;
  return object;
};

var hasOwnProperty = {}.hasOwnProperty;
var _has = function (it, key) {
  return hasOwnProperty.call(it, key);
};

var PROTOTYPE = 'prototype';

var $export = function (type, name, source) {
  var IS_FORCED = type & $export.F;
  var IS_GLOBAL = type & $export.G;
  var IS_STATIC = type & $export.S;
  var IS_PROTO = type & $export.P;
  var IS_BIND = type & $export.B;
  var IS_WRAP = type & $export.W;
  var exports = IS_GLOBAL ? _core : _core[name] || (_core[name] = {});
  var expProto = exports[PROTOTYPE];
  var target = IS_GLOBAL ? _global : IS_STATIC ? _global[name] : (_global[name] || {})[PROTOTYPE];
  var key, own, out;
  if (IS_GLOBAL) source = name;
  for (key in source) {
    // contains in native
    own = !IS_FORCED && target && target[key] !== undefined;
    if (own && _has(exports, key)) continue;
    // export native or passed
    out = own ? target[key] : source[key];
    // prevent global pollution for namespaces
    exports[key] = IS_GLOBAL && typeof target[key] != 'function' ? source[key]
    // bind timers to global for call from export context
    : IS_BIND && own ? _ctx(out, _global)
    // wrap global constructors for prevent change them in library
    : IS_WRAP && target[key] == out ? (function (C) {
      var F = function (a, b, c) {
        if (this instanceof C) {
          switch (arguments.length) {
            case 0: return new C();
            case 1: return new C(a);
            case 2: return new C(a, b);
          } return new C(a, b, c);
        } return C.apply(this, arguments);
      };
      F[PROTOTYPE] = C[PROTOTYPE];
      return F;
    // make static versions for prototype methods
    })(out) : IS_PROTO && typeof out == 'function' ? _ctx(Function.call, out) : out;
    // export proto methods to core.%CONSTRUCTOR%.methods.%NAME%
    if (IS_PROTO) {
      (exports.virtual || (exports.virtual = {}))[key] = out;
      // export proto methods to core.%CONSTRUCTOR%.prototype.%NAME%
      if (type & $export.R && expProto && !expProto[key]) _hide(expProto, key, out);
    }
  }
};
// type bitmap
$export.F = 1;   // forced
$export.G = 2;   // global
$export.S = 4;   // static
$export.P = 8;   // proto
$export.B = 16;  // bind
$export.W = 32;  // wrap
$export.U = 64;  // safe
$export.R = 128; // real proto method for `library`
var _export = $export;

// 19.1.2.4 / 15.2.3.6 Object.defineProperty(O, P, Attributes)
_export(_export.S + _export.F * !_descriptors, 'Object', { defineProperty: _objectDp.f });

var $Object = _core.Object;
var defineProperty$2 = function defineProperty(it, key, desc) {
  return $Object.defineProperty(it, key, desc);
};

var defineProperty = createCommonjsModule(function (module) {
module.exports = { "default": defineProperty$2, __esModule: true };
});

unwrapExports(defineProperty);

var createClass = createCommonjsModule(function (module, exports) {
exports.__esModule = true;



var _defineProperty2 = _interopRequireDefault(defineProperty);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = function () {
  function defineProperties(target, props) {
    for (var i = 0; i < props.length; i++) {
      var descriptor = props[i];
      descriptor.enumerable = descriptor.enumerable || false;
      descriptor.configurable = true;
      if ("value" in descriptor) descriptor.writable = true;
      (0, _defineProperty2.default)(target, descriptor.key, descriptor);
    }
  }

  return function (Constructor, protoProps, staticProps) {
    if (protoProps) defineProperties(Constructor.prototype, protoProps);
    if (staticProps) defineProperties(Constructor, staticProps);
    return Constructor;
  };
}();
});

var _createClass = unwrapExports(createClass);

// ================ 一些常量 ======================
// ## 渲染之后的 图片容器类名
var upViewClass = 'upload-view';
// ## loading效果的类名
var upLoadingClass = 'upload-loading';
// ## 关闭按钮的类名
var upCloseClass = 'upload-close';
// ## 遮罩层，替换样式类名
var upMaskClass = 'upload-mask';
// ## img，video标签的类名
var upImageClass = 'upload-image';
// ## 多选上传的最外层类名
var upMultipleContainerClass = 'upload-multiple-container';
// ## 多图上传 view的类名
var upMultipleWrapperClass = 'upload-multiple-wrapper';
// ## 多选上传的上传按钮
var upMultipleClass = 'upload-multiple';
// ## 多选上传的上传按钮，达到限制的个数隐藏样式的类名
var upAddClass = 'upload__add--fly';
// ## js代码是预先渲染基础结构，并添加标志，上传成功后，find对应的id标志，渲染img、video
var attrId = 'data-upload-id';
// ## 隐藏input的标志，多选上传时会克隆
var inputSelector = 'input[data-upload="upload_data"]';
// ## jQuery 事件命名空间
var eventSuffix = '.upload0123';
// ## 通知对应的自定义事件，事件尾缀
var triggerEvent = '.os';
// ## 自定义文件夹，这个是测试的，上线后需要更改
var dirPrefix = 'mengmeng-test';
// ## jQuery data 缓存的 key值
var dataCache = 'ossDirect*';

/**
 * @随机生成文件名
 * @example `Y0nt6x5wl498jYwW`
 * @param {Number} len
 */
function randomString(len) {
  len = len || 32;
  var chars = 'ABCDEFGHIJKLMNOPRSTUYZabcdefghijklmnoprstuyz';
  var maxPos = chars.length;
  var pwd = '';
  for (var i = 0; i < len; i++) {
    // ~~ 等同于 Math.floor
    pwd += chars.charAt(~~(Math.random() * maxPos));
  }
  return pwd;
}

/**
 * @desc 获取文件后缀名
 * @example '.png'、'png'
 * @param {String} filename
 * @param {Boolean} clearDot 是否去除后缀名中的点
 */
function getSuffix() {
  var filename = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '';
  var clearDot = arguments[1];

  var pos = filename.lastIndexOf('.');
  var suffix = '';
  if (pos !== -1) {
    suffix = filename.substring(clearDot ? pos + 1 : pos);
  }
  return suffix;
}

/**
 * @desc 计算生成的文件路径 `${dir}${folder}/${随机生成的文件名}`
 * @example `mengmeng/web-test-images/Y0nt6x5wl498jYwW1545547889052.jpg`
 * @param {String} dir 服务端返回的存放目录名
 * @param {String} folder 文件夹名
 * @param {String} filename
 */
function calcObjectName() {
  var dir = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '';
  var folder = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : dirPrefix + '-images/';
  var filename = arguments[2];

  // const oName = folder + randomString(16) + (new Date() * 1);
  var oName = folder + randomString(25);
  return dir + oName + getSuffix(filename);
}

/**
 * @desc 获取上传文件的类型
 * @param {videos|images} type
 * @param {Boolean} disableGif
 */
function getAccept(type) {
  return type === 'videos' ? '.mpg,.m4v,.mp4,.flv,.3gp,.mov,.avi,.rmvb,.mkv,.wmv' : '.jpg,.jpeg,.png,.bmp,.webp';
}

/**
 * @desc 时间戳
 */
function timestamp() {
  return Date.parse(new Date()) / 1000;
}

/**
 * @desc 获取路径问号前面的值
 */
function trueValue() {
  var src = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '';

  return src.split('?')[0];
}

/**
 * @desc 获取链接文件名
 */
function getFilename(src) {
  src = trueValue(src);
  var divide = src.lastIndexOf('/');
  if (divide >= 0) {
    src = src.substring(divide + 1);
  }
  return src;
}

/**
 * @param {Number} resourceDuration 上传视频的资源时长
 * @param {Number} limitDuration 限制的时长
 * @param {String} s
 */
function duration(resourceDuration) {
  var limitDuration = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0;
  var s = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : '秒';

  if (limitDuration && resourceDuration > limitDuration) {
    $.alertModal('error', {
      text: '\u5141\u8BB8\u4E0A\u4F20\u89C6\u9891\u65F6\u957F\uFF1A' + limitDuration + s
    });
    return false;
  }
}

/**
 * @desc 暴露随机命名接口
 */
$.calcObjectName = calcObjectName;

var Q = window.Q;
var layer = window.layer;
var formatSize = Q.formatSize;

var host = '';
var policyBase64 = '';
var accessid = '';
var signature = '';
var expire = 0;
var callbackbody = null;
var key = '';
var objectName = '';
var now = timestamp();

function fileSize(accept, suffix, size, maxSize) {
  var text = arguments.length > 4 && arguments[4] !== undefined ? arguments[4] : '允许上传图片大小：';

  if (accept.indexOf(suffix) >= 0) {
    if (size > maxSize) {
      $.alertModal('error', {
        text: '' + text + formatSize(maxSize)
      });
      return false;
    }
  }
}

var OssDirectSend = function () {
  function OssDirectSend(View) {
    _classCallCheck(this, OssDirectSend);

    // 视图注册
    this.View = View;

    var options = this.View.options;
    var _type = options.type || '';

    // 默认禁用了 gif，参数 gif && 参数type 设置可以上传images
    if (options.gif && _type.indexOf('images') >= 0) {
      View.imagesAccept += ',.gif';
      delete options.disallows;
    }
    /**
     * 默认上传图片，type === 'images'
     * 上传视频，type === 'videos'
     * 同时上传图片、视频，type === 'images,videos'
     */
    if (_type === 'images,videos') {
      options.allows = View.imagesAccept + ',' + View.videosAccept;
    }

    options.videosAccept = View.videosAccept;
    options.imagesAccept = View.imagesAccept;
    this.__createUploader(options);
  }

  _createClass(OssDirectSend, [{
    key: '__createUploader',
    value: function __createUploader(options) {
      // 变量保存
      var self = this;
      this.uploader = new Q.Uploader($.extend(true, {}, options, {
        // url: 'http://oss.aliyuncs.com',
        on: {
          init: function init() {
            if (!this.ossKeys) {
              this.ossKeys = {};
            }
            self.View.$view.trigger('init' + triggerEvent);
          },
          addAsync: function addAsync(task, callback) {
            var ops = this.ops;
            if (task.limited === 'ext') {
              $.alertModal('error', {
                text: '\u5141\u8BB8\u4E0A\u4F20\u7684\u6587\u4EF6\u683C\u5F0F\uFF1A' + ops.allows
              });
              return false;
            }

            // 上传视频大小限制
            /**
             * TODO
             * 上传视频大小限制参数
             */
            // const video = fileSize(ops.videosAccept, task.ext, task.size, self.View.videosMaxSize, '允许上传视频大小：');
            var video = fileSize(ops.videosAccept, task.ext, task.size, ops.videosMaxSize, '允许上传视频大小：');
            if (video === false) {
              return video;
            }

            // 上传图片大小限制
            var image = fileSize(ops.imagesAccept, task.ext, task.size, self.View.imagesMaxSize);
            if (image === false) {
              return image;
            }

            // 上传视频时长限制
            var ret = duration(task.videoSecond, ops.videoSecond);
            if (ret === false) {
              return ret;
            }

            // 计数
            if (ops.multiple && ops.max) {
              if (self.View.count$ >= ops.max) {
                return false;
              }
              self.View.count$++;
            }

            // 获取签名
            self.addAsyncGetSignature(callback);
          },
          uploadAsync: function uploadAsync(task, callback) {
            this.ops.layerIndex !== null && layer.close(this.ops.layerIndex);
            var filename = task.name;
            self.uploadAsyncSetParams(filename);

            // 将随机生成的自定义文件名保存起来
            // 在 complete 上传完成中返回给视图
            if (!this.ossKeys[filename]) {
              this.ossKeys[filename] = objectName;
              // loading
              self.View.preRenderLoading(objectName, filename);
              // 异步回调
              callback();
              self.View.$view.trigger('upload' + triggerEvent);
            }
          },
          complete: function complete(task) {
            var _this = this;

            if (task) {
              if (task.state === 2) {
                // 图片出不来延时看看
                setTimeout(function () {
                  var ret = _this.ops.url + '/' + _this.ossKeys[task.name];
                  self.View.render(ret, _this, task.name);
                  self.View.$view.trigger('complete' + triggerEvent);
                }, 60);
              } else {
                self.View.$view.trigger('error' + triggerEvent);
                self.__fail(null, task);
              }
            }
          }
        }
      }));
    }
  }, {
    key: 'addAsyncGetSignature',
    value: function addAsyncGetSignature(upCallback) {
      var _this2 = this;

      now = timestamp();
      // http://oss.ilashou.com/oss/authorization?name=mm
      // http://ffn9t9.natappfree.cc/oss/authorization?name=mm
      // =====================================
      // ===== 当时间戳过期后从新请求签名，
      // ===== 然后对expire从新赋值
      // =====================================
      if (expire < now + 3) {
        $.ajax({
          type: 'GET',
          url: 'https://oss.ilashou.com/oss/authorization?name=mm',
          dataType: 'json',
          // ie is SB
          cache: false
        }).done(function (res) {
          var data = res.data;
          if (res.code === 1200 && !!data) {
            data = JSON.parse(data);
            // 对expire从新赋值
            expire = parseInt(data.expire);
            key = data.dir;
            policyBase64 = data.policy;
            accessid = data.accessid;
            signature = data.signature;
            callbackbody = data.callback;
            host = data.host;
            // 调用upCallback
            _this2.__addSync(upCallback);
          } else {
            _this2.__fail(upCallback, res);
          }
        }).fail(function (erro) {
          _this2.__fail(upCallback, erro);
        });
      } else {
        // 使用已请求的签名
        this.__addSync(upCallback);
      }
    }
  }, {
    key: '__fail',
    value: function __fail(upCallback, msg) {
      // 若 参数为false，该任务不会上传
      upCallback && upCallback(false);
      $.alertModal('error', { text: '服务器异常，请重试！' });
      console.log('signRequest error: ', msg);
    }
  }, {
    key: '__addSync',
    value: function __addSync(upCallback) {
      // 设置 host
      this.uploader.set({ url: host });
      upCallback();
      this.View.$view.trigger('add' + triggerEvent);
    }
  }, {
    key: 'uploadAsyncSetParams',
    value: function uploadAsyncSetParams(filename) {
      var folder = this.View.getFolder(filename);
      objectName = calcObjectName(key, folder, filename);
      var data = {
        'Filename': objectName,
        'key': objectName,
        'policy': policyBase64,
        'OSSAccessKeyId': accessid,
        'success_action_status': '200',
        'callback': callbackbody,
        'signature': signature
      };

      // callbackbody == undefined 时删除，不然参数不对
      if (!data.callback) {
        delete data.callback;
      }
      // 设置上传参数
      this.uploader.set({ data: data });
    }
  }]);

  return OssDirectSend;
}();

var notSupportFR = typeof FileReader !== 'function';

var RenderView = function () {
  function RenderView(Button) {
    var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    _classCallCheck(this, RenderView);

    this.videosAccept = getAccept('videos');
    this.imagesAccept = getAccept('images');
    this.videosFolder = dirPrefix + '-videos/';
    this.imagesFolder = dirPrefix + '-images/';
    this.videosMaxSize = options.videosMaxSize || 20 * 1024 * 1024;
    this.imagesMaxSize = options.imagesMaxSize || 10 * 1024 * 1024;
    this.background = true;

    this.__init(Button, options);

    // 已经上传的个数
    this._count = this.$view.find('.' + upViewClass).length;
    if (options.multiple) {
      this.count$ = this._count;
    }
    // 有的是直接上传，需要初始化 oss直传，
    // 有的是点击后弹框后选择上传方式，这时候不需要初始化，需要外部调用 setOption()
    this.oss = options.initOss ? new OssDirectSend(this) : null;
  }

  _createClass(RenderView, [{
    key: 'setOption',
    value: function setOption() {
      var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
      var newOss = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;

      this.options = $.extend(this.options, options);
      if (options.type === 'videos' && !options.allows) {
        this.options.allows = getAccept(options.type);
      }
      if (newOss) {
        this.oss = new OssDirectSend(this);
      }
    }
  }, {
    key: 'clearCount',
    value: function clearCount() {
      this.$view.find('.' + upViewClass).off().remove();
      this.count$ = 0;
    }
  }, {
    key: '__init',
    value: function __init(Button, options) {
      var _this = this;

      delete options.upName;

      var defaults = {
        type: 'images', // 'videos'
        multipleUpdate: false,
        max: 5, // 限制个数
        onchange: null,
        // 视频时长 秒
        videoSecond: 180,
        // 视频时长 分钟
        videoMinute: 0,
        // 视频时长 小时，0表示不限制
        videoHour: 0,
        // 关闭layer
        layerIndex: null,
        // 以下是上传插件内置的参数
        target: Button,
        upName: 'file',
        dataType: 'xml',
        // container: document.body,
        allows: this.imagesAccept,
        multiple: true,
        html5: true,
        maxSize: 0, // 限制大小
        disallows: '.gif' // 默认禁用 gif上传
      };

      if (Button) {
        defaults.container = Button.parentNode;
      }

      this.options = $.extend(defaults, options);
      this.$view = options.$view || $(options.view);

      var ops = this.options;
      // 多选，和 多选中的替换，计算个数
      if (this.options.multiple || this.options.multipleUpdate) {
        var $parents = this.$view.parents('.' + upMultipleContainerClass);
        this.$add = options.$add || $parents.find('.' + upMultipleClass);
        this.$addBtn = $parents.find('#continueAdd');
      }

      // 回显时，初始化，是否可修改
      if (ops.multiple) {
        this.$inputClone = this.$view.find(inputSelector).eq(0);
        var $disaply = this.$view.find('.' + upViewClass);
        this.$view.data('not') !== 'mask' && this.multipleReplace($disaply);
      }

      if (this.options.multipleUpdate) {
        setTimeout(function () {
          _this.$parentData = _this.$view.parents('.' + upMultipleWrapperClass).getDirect();
        });
      }

      this.bindEvents();
    }
  }, {
    key: 'createElement',
    value: function createElement() {
      var options = this.options;
      var $viewTempl = $('<div class="' + upViewClass + '"></div>');
      !options.notLoading && $viewTempl.append('<p class="' + upLoadingClass + '"></p>');
      !options.notClose && $viewTempl.append('<span class="' + upCloseClass + '"></span>');

      if (!options.notMask && options.type !== 'videos') {
        $viewTempl.append('<div class="' + upMaskClass + '"><p></p></div>');
      }

      if (options.multiple) {
        $viewTempl.append(this.$inputClone.clone(true));
      }

      return $viewTempl;
    }

    /**
     * @desc 先添加loading效果，这个在 oss-web-uploads.js 中调用
     */

  }, {
    key: 'preRenderLoading',
    value: function preRenderLoading(src, filename) {
      var options = this.options;
      var dataId = this.getId(src, filename);

      if (!options.multiple) {
        var $view = options.multipleUpdate ? this.$view : this.$view.find('.' + upViewClass);
        if ($view.length) {
          $view.attr(attrId, dataId);
          $view.append('<p class="' + upLoadingClass + '"></p>');
        } else {
          this.$view.append(this.createElement().attr(attrId, dataId));
        }
      } else {
        this.$view.append(this.createElement().attr(attrId, dataId));
        this.$view.find('> ' + inputSelector).remove();
      }
    }
  }, {
    key: 'render',
    value: function render(src, up, filename) {
      var _this2 = this;

      var options = this.options;
      src = trueValue(src);
      if (this.isImagesFolder(src, filename)) {
        if (!this.background && src.lastIndexOf('.gif') === -1) {
          // 不是gif 裁剪
          src = src + this.ossClip;
        }
        var img = new window.Image();
        img.onload = img.onabort = function () {
          // 资源加载完操作
          // 图片出错了请重新上传！
          _this2.append(img.src || '', up, filename, true);
          img = null;
        };

        img.onerror = function () {
          _this2.append(img.src || '', up, filename, true, true);
          img = null;
        };

        img.src = src;
      } else {
        this.append(src, up, filename, false);
      }
      // Onchange Callback
      typeof options.onchange === 'function' && options.onchange(src);
    }
  }, {
    key: 'getId',
    value: function getId() {
      var src = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '';
      var filename = arguments[1];

      var s = this.getFolder(filename);
      return trueValue(src).split(s)[1] || getFilename(src);
    }

    // 根据文件名获取文件夹

  }, {
    key: 'getFolder',
    value: function getFolder(filename) {
      return this.imagesAccept.indexOf(getSuffix(filename).toLocaleLowerCase()) >= 0 ? this.imagesFolder : this.videosFolder;
    }

    // 是否是图片

  }, {
    key: 'isImagesFolder',
    value: function isImagesFolder(src, filename) {
      return this.getFolder(filename || getFilename(src)) === this.imagesFolder;
    }
  }, {
    key: 'ie9Canplaythrough',
    value: function ie9Canplaythrough($dom, $view, src, isImage) {
      var self = this;
      var options = this.options;

      if (notSupportFR && !isImage) {
        $dom.on('canplaythrough', function () {
          // const hour = parseInt(this.duration / 3600);
          // const minute = parseInt((this.duration % 3600) / 60);
          // ~~ 等同于 Math.floor()
          var second = ~~(this.duration % 60);
          var ret = duration(second, options.videoSecond);
          if (ret === false) {
            $dom.remove();
            $view.find('.' + upCloseClass).trigger('click' + eventSuffix);
            $dom = $view = null;
          } else {
            self.__triggerRander($view, src, isImage);
          }
        });
      }
    }
  }, {
    key: '__triggerRander',
    value: function __triggerRander($uploadView, src, isImage) {
      $uploadView.trigger('render' + triggerEvent, src);
      // 触发页面上的 ajax提交，保存到相册数据库
      $('head').trigger('render' + triggerEvent, {
        url: trueValue(src),
        type: isImage ? 0 : 1
      });
    }
  }, {
    key: 'append',
    value: function append(src, up, filename, isImage, error) {
      var options = this.options;
      var dataId = this.getId(src, filename);
      var multipleUpdate = options.multipleUpdate;
      var $uploadView = multipleUpdate ? this.$view : this.$view.find('[' + attrId + '="' + dataId + '"]');
      var $dom = null;

      if (isImage) {
        if (!error) {
          $dom = this.background ? $('<p></p>').css('background-image', 'url(' + src + ')').data('url', '' + src) : $('<img src="' + src + '" alt="">');
        } else {
          var errText = '图片加载出错了，请从新上传！';
          $dom = this.background ? $('<p class="upload-error">' + errText + '</p>') : $('<img class="upload-error" src="" alt="' + errText + '">');
        }
      } else {
        $dom = $('<video src="' + src + '"></video>');
      }

      $dom.addClass(upImageClass);
      var local = !!up && !!filename; // 从本地上传
      local && this.ie9Canplaythrough($dom, $uploadView, src, isImage);

      if (!options.multiple) {
        // const $image = $uploadView.find(`.${upImageClass}`);
        // if ($image.length) {
        //   $image.attr('src', src);
        // } else {
        //   $uploadView.append($dom);
        // }

        /**
         * 采用每次都移除重写append，
         * 因为有同时可以上传图片和视频，
         */
        $uploadView.find('.' + upImageClass).remove();
        $uploadView.append($dom);
        this.setValue(multipleUpdate ? $uploadView.find(inputSelector) : null, src);
      } else {
        $uploadView.append($dom);
        this.setValue($uploadView.find(inputSelector), src);
        // 多图上传点击单个是替换从新实例化 RenderView
        if ($uploadView.parent().data('not') !== 'mask') {
          this.multipleReplace($uploadView);
        }
      }

      // 移除loading...
      this.removeLoading($uploadView);
      if (local) {
        // 这个是从本地上传，传过来的参数，计数在上传之前计算了，
        delete up.ossKeys[filename];
        // 支持 FileReader API时，或者是图片时直接触发render事件，否则走 ie9Canplaythrough
        if (!notSupportFR || isImage) {
          this.__triggerRander($uploadView, src, isImage);
        }
      } else if (options.multiple) {
        // 从我的相册的时候选择，要计数
        this.count$++;
      }
    }

    /**
     * @desc 多图上传后，如果单个有替换功能，继续调用 $.fn.ossDirect实例化
     */

  }, {
    key: 'multipleReplace',
    value: function multipleReplace($node) {
      // 多选，添加后，
      if ($node && $node.length) {
        $node.ossDirect({
          multiple: false,
          multipleUpdate: true
        });
        // 重新添加替换功能
        var type = this.options.type;
        var list = type === 'videos' ? [{
          href: 'javascript:;',
          icon: 'icon-xiangce',
          text: '我的视频'
        }] : null;
        var self = this;
        $node.find('.' + upMaskClass).off().on('click' + eventSuffix, function () {
          var $view = $(this).parent();
          var _ops = self.options;
          // console.log(_ops);

          // 继承固定传参
          $.selectUploadWay(list, {
            $view: $view,
            multiple: false,
            allows: _ops.allows,
            disallows: _ops.disallows,
            clipHeight: _ops.clipHeight,
            clipWidth: _ops.clipWidth,
            gif: _ops.gif,
            type: _ops.type,
            url: _ops.url,
            url1: _ops.url1,
            videoHour: _ops.videoHour,
            videoMinute: _ops.videoMinute,
            videoSecond: _ops.videoSecond
          });
        });
      }
    }
  }, {
    key: 'removeLoading',
    value: function removeLoading($uploadView) {
      if ($uploadView && $uploadView.length) {
        $uploadView.find('.' + upLoadingClass).remove();
      } else {
        this.$view.find('.' + upLoadingClass).remove();
      }
    }

    /**
     * @desc 绑定一些事件
     */

  }, {
    key: 'bindEvents',
    value: function bindEvents() {
      var _this3 = this;

      // 关闭移除事件
      this.$view.off('click' + eventSuffix).on('click' + eventSuffix, '.' + upCloseClass, function (e) {
        var $target = $(e.target);
        $target.parent().remove();
        _this3.setIconfont(true);

        if (_this3.options.multiple) {
          if (_this3.count$ <= 1) {
            _this3.$view.append(_this3.$inputClone);
            _this3.setValue(_this3.$inputClone, '');
          }
        } else {
          _this3.setValue(null, '');
        }

        if (_this3.options.multiple) {
          _this3.count$--;
        } else if (_this3.options.multipleUpdate) {
          !!_this3.$parentData && _this3.$parentData.count$--;
        }
        return false;
      });
    }

    /**
     * @desc 隐藏 上传iconfont
     */

  }, {
    key: 'setIconfont',
    value: function setIconfont(flag) {
      this.$view.find('.iconfont').toggleClass('hide', flag);
    }

    /**
     * @desc 设置隐藏input的值，需要验证的就验证一下
     * @param {jQuery DOM} $input
     * @param {String} value 设置隐藏input的值
     */

  }, {
    key: 'setValue',
    value: function setValue($input) {
      var value = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '';

      $input = $input || this.$view.find(inputSelector);
      value = trueValue(value);

      $input.val(value);
      this.setIconfont();

      if ($input.parents('form').length) {
        $input.valid();
      }
    }

    /**
     * @desc
     * 弹出选择上传方式框时，当弹框关闭时，
     * 清理 this.oss 引用的new OssDirectSend(this)实例
     */

  }, {
    key: 'destroy',
    value: function destroy() {
      if (!this.options.initOss) {
        this.oss = null;
      }
    }
  }, {
    key: 'count$',
    get: function get() {
      // this.$view.find(`.${upViewClass}`).length
      return this._count;
    },
    set: function set(c) {
      this._count = c;
      if (this.$add && this.$add.length) {
        // 隐藏上传按钮
        this.$add.toggleClass(upAddClass, this.count$ >= this.options.max);
      }
      if (this.$addBtn && this.$addBtn.length) {
        // 隐藏继续上传按钮
        this.$addBtn.toggleClass(upAddClass, this.count$ >= this.options.max);
      }
    }
  }, {
    key: 'ossClip',
    get: function get() {
      // ossClip
      var options = this.options;
      var $view = this.$view;

      if (!options.clips && !this.background) {
        var w = options.clipWidth || $view.innerWidth();
        var h = options.clipHeight || $view.innerHeight();
        return '?x-oss-process=image/format,' + (options.clipFormat || 'png') + '/resize,m_pad,w_' + w + ',h_' + h + ',color_f5f5f5';
      } else {
        return options.clips || '';
      }
    }
  }]);

  return RenderView;
}();

// 对外接口，获取实例
// options 可以设置，回显的时候需要一些参数
$.fn.getDirect = function (options) {
  var data = this.data(dataCache);
  // 设置 options
  if (options) {
    $.extend(data.options, options || {});
  }
  return data;
};

$.fn.ossDirect = function (options) {
  this.each(function () {
    var $this = $(this);
    var data = $.data(this, dataCache);
    // 有这个按钮，表示直接上传，反之是弹框在选择
    var button = $this.find('.single-upload').get(0);
    // 表示回显不可编辑
    var readonly = $this.attr('readonly');

    if (!data && !readonly) {
      $.data(this, dataCache, new RenderView(button, $.extend(options, {
        view: this,
        initOss: !!button
      })));
    }
  });
};
// 头像，logo上传
$('[data-upload="single"]').ossDirect({
  multiple: false,
  imagesMaxSize: 5 * 1024 * 1024
});

$('.upload-multiple-wrapper').ossDirect({
  multiple: true,
  imagesMaxSize: 5 * 1024 * 1024
});

}());
;

(function (window, $, document, undefined) {
  // 静态方法工具方法
  $.extend({
    beforeZero: function (obj) {
      return obj < 10 ? ('0' + obj) : obj;
    },

    dashJoin: function () {
      var args = [].slice.apply(arguments);
      return args.join(args[3]);
    },

    dateDashJoin: function (year, month, day, join) {
      join = join || '';
      var m = $.beforeZero(month);
      var d = $.beforeZero(day);
      return $.dashJoin(year, m, d, join);
    },

    isInIframe: function () {
      return window.frames.length !== parent.frames.length;
    },

    tryJSON: function (str) {
      try {
        var obj = JSON.parse(str);
        return (typeof obj === 'object' && obj) ? obj : str;

      } catch (e) {
        return str;
      }
    },

    getRange: function (input) {
      if (typeof input.selectionStart === 'number' && typeof input.selectionEnd === 'number') {
        return {
          start: input.selectionStart,
          end: input.selectionEnd
        };
      }

      var range = document.selection.createRange();
      var start = 0;
      var end = 0;

      if (range && range.parentElement() === input) {
        var len = input.value.length;
        var normalizeValue = input.value.replace(/\r\n/g, '\n');

        var textInputRange = input.createTextRange();
        textInputRange.moveToBookmark(range.getBookmark());

        var endRange = input.createTextRange();
        endRange.collapse(false);

        if (textInputRange.compareEndPoints('StartToEnd', endRange) > -1) {
          start = end = len;
        } else {
          start = -textInputRange.moveStart('character', -len);
          start += normalizeValue.slice(0, start).split('\n').length - 1;

          if (textInputRange.compareEndPoints('EndToEnd', endRange) > -1) {
            end = len;
          } else {
            end = -textInputRange.moveEnd('character', -len);
            end += normalizeValue.slice(0, end).split('\n').length - 1;
          }
        }
      }

      return {
        start: start,
        end: end
      };
    },

    setRange: function (input, start, end) {
      end = end || start;

      if (input.setSelectionRange) {
        input.setSelectionRange(start, end);
        input.focus();

      } else if (input.createTextRange) {
        var range = input.createTextRange();
        range.collapse();
        range.moveStart('character', start);
        range.moveEnd('character', end - start);
        range.select();
      }
    },

    /**
     * IE9, 需要保存 range
     */
    updateRange: function (input, attr) {
      attr = attr || 'data-range';
      var range = $.getRange(input);
      input.setAttribute(attr, range.start);
      return range;
    },
    layerInterface: function (conf) {
      var params = {
        anim: 0, // 动画
        move: false, // 禁止移动
        shade: 0.15, // 遮罩层透明掉
        shadeClose: true, // 点击遮罩层关闭弹框
      };
  
      return layer.open( $.extend(params, conf || {}) );
    },
     //显示原始图片弹框
  showOriginalImg: function(width,height,src,endCallback) {
    return $.layerInterface({
      title: false,
      type: 1,
      skin: 'original-image',
      area: [width+'px',height+'px'],
      shade: 0.3,
      end: endCallback,
      content: '<div class="image-container">\
        <img src="'+src+'">\
      </div>'
    });
  },

  //点击显示原图
  showOriginalImage: function(parent,child) {
    var flag = true;
    if(flag) {
      $(parent).on('click', child, function(){
        flag = false;
        var image = new Image();
        var path = $(this).css("backgroundImage");
        var src = $(this).data('photo') ? $(this).data('photo') :
          $(this).attr('src') ? $(this).attr('src') : 
            $(this).css("backgroundImage").match(/(http).*?(?=(\?x-oss))/)[0];
        // var src = path.indexOf('?x-oss') > -1 ?
        //           path.match(/(http).*?(?=(\?x-oss))/)[0] :
        //           path.match(/(http).*?(?=")/)[0];
        var index = /\?type=mjsuo/g.test(src) ? src.indexOf('?type=mjsuo') : src.indexOf('?x-oss');
        var _index = /^http/.test(src) ? layer.load() : null;
        $('.layui-layer-shade').on('click',function(){
          _index && layer.close(_index);
        })
        var originalSrc = index > -1 ? src.substring(0,index) : src;
        image.src = originalSrc;
        image.onload=function() {
          var screenH = $(window).height();
          var w = image.width > 1096 ? 1096 : image.width;
          var h = image.width > 1096 ? 'auto' : image.height;
          if((image.width >= 1096 && 1096/image.width >= screenH/image.height) 
              || (image.width<1096 && image.height>screenH)) {
            h = 0.9*screenH;
            w = 0.9*image.width/image.height*screenH;
          }
          _index && layer.close(_index); 
          $.showOriginalImg(w,h,originalSrc,function(){
            flag = true;
          });
        } 
      })
    }
  },
  //回到顶部
  goTop: function() {
    if ( $.isInIframe() ) {
      // 在 iframe 页面中 return
      return;
    }
    if ( $('.fixed-wrap').length==0 ) {
      return;
    }
    console.log(123)
    $(document.body).on('click.goTop', '.fixed-wrap > .go-top', function () {
      $('html,body').animate({ scrollTop: 0 }, 450);
    });

    // var $multiUtil = $('#multiUtil').length ? $('#multiUtil') : $('<div id="multiUtil" class="multi-util"><a class="scroll-top" href="javascript:;"><i class="iconfont icon-zhiding"></i></a></div>');

    $(window).on('scroll.goTop', function (e) {
      var $body = $(document.body);
      var winScrollTop = $(this).scrollTop() || $body.scrollTop();
      var winH = $(this).height();
      var top = $('footer.layout-footer').offset().top;

      var wwt = winScrollTop + winH - top;
      var bottom = wwt >= 0 ? wwt : 0;
      var $elem = $body.find('.fixed-wrap>.go-top');
      if (winScrollTop >= winH * 1.2) {
        // var $elem = $body.find('.fixed-wrap>.go-top');
        // !$elem.length ? $body.append($multiUtil) : $elem.show('fast');
        $elem.show(150);
        
      }else {
        // $body.find('#multiUtil').hide();
        $elem.hide(150);
      }

      $body.find('.fixed-wrap').css('bottom', bottom + 250);
      $body.find('.warm-prompt-wrap').css('bottom', bottom + 10);

    }).trigger('scroll.goTop');
  }
});

  // 原型方法
  jQuery.fn.extend({
    /**
     * @desc 代替锚点，如果不支持就使用锚点
     */
    scrollIntoView: function (view) {
      var view = (view === undefined) ? true : view;
      var elem = this[0];
      'scrollIntoView' in elem ? elem.scrollIntoView(view) : window.location.hash = this.attr('id');
      return this;
    },

    /**
     * @desc 多行文本省略号
     */
    multiTextEllipsis: function (config) {
      function __ellipsis($elem, options) {
        $elem.next('a').hide();

        if ($elem.data('drop')) {
          var $text = $elem.text();

          $elem.next('a').on('click', function () {
            $elem.text($text);
            var _this = $(this);
            var target = _this.data('target') ? $(_this.data('target')) : $elem.parent();
            target.css({
              'height': 'auto'
            });
            _this.hide();
            return false;
          });
        }

        var max = options.maxWidth || $elem.data('zip-width') || $elem.width(),
          ellipsis_char = options.ellipsisChar || $elem.data('zip-char') || '......',
          maxLine = options.maxLine || $elem.data('zip-row') || 2;

        max = max * (+maxLine);

        var text = $elem.text().trim().replace(' ', '　'); //for fix white-space bug

        var $char = $('<span>' + ellipsis_char + '</span>').appendTo(document.body);
        var $temp_elem = $elem.clone(false)
          .css({
            "position": "absolute",
            "visibility": "hidden",
            "whiteSpace": "nowrap",
            "width": "auto",
            "display": "inline",
            "opacity": 0

          }).appendTo(document.body);

        var char_width = $char.width() / 2;
        var width = $temp_elem.width();

        if (width > max) {
          var stop = Math.floor(text.length * max / width); // 极限停止
          var temp_str = text.substring(0, stop) + ellipsis_char;
          width = $temp_elem.text(temp_str).width();

          if (width > max) {
            while (width > max && stop > 1) {
              stop--;
              temp_str = text.substring(0, stop) + ellipsis_char;
              width = $temp_elem.text(temp_str).width();
            }

          } else if (width < max) {
            while (width < max && stop < text.length) {
              stop++;
              temp_str = text.substring(0, stop) + ellipsis_char;
              width = $temp_elem.text(temp_str).width();
            }

            if (width > max) {
              temp_str = text.substring(0, stop - 1) + ellipsis_char;
            }
          }

          $elem.text(temp_str.replace('　', ' '));
          $elem.next('a').show();
        } else {
          $elem.next('a').hide();
        }

        $char.remove();
        $temp_elem.remove();
      }

      return this.each(function () {
        var $this = $(this);
        __ellipsis($this, config || {});
      });
    },
    //多行文本省略号
    tooMuch:function(rowNumber,noTitle){
      this.each(function() {
        var null_character='',_this=this;
        var innerText_pseudoArray=new String(this.innerText);
        for(i=0;i<innerText_pseudoArray.length;i++){
          null_character+='<i>'+innerText_pseudoArray[i]+'</i>';
        }
        this.innerHTML=null_character;
        var swl_bodyWidth = $(this).width();
        var swlb_itemCollectionWidth = 0,swlb_itemStr='';
        $(this).children().each(function() {
          swlb_itemCollectionWidth += this.offsetWidth;
          if(swlb_itemCollectionWidth >((swl_bodyWidth-16)*rowNumber)) {
            if(!noTitle)_this.title=_this.innerText;						
            _this.innerHTML=swlb_itemStr+'...';
            _this.style.opacity=1
            return false
          }
          swlb_itemStr+=this.innerText;
        });
        this.innerHTML=this.innerText;
        this.style.opacity=1
      });
    },

    //代替title属性
    myTitle:function(){
      var currentIndex=0;
      $(this).on('mouseenter',function () {
        // var width = $(this).outerWidth(),
        var width = $(this)[0].scrollWidth,
            fixedWidth=parseInt( $(this).css('width') );
        var maxWidth = parseInt( $(this).css('max-width') )||fixedWidth;
        if (width > maxWidth) {
          layer.tips($(this).text(), this, {
              tips: [3,'#fff'],
              skin:'tips-menu',
              area: [maxWidth+'px','auto'],
              time: 0,
              success:function(layero,index){
                currentIndex=index;
              }
            })
        }
      }).on('mouseleave', function(event) {
        layer.close(currentIndex);
      });
    },

    //自动添加title属性
    autoTitle:function(){
      var width=0,fixedWidth=0,maxWidth=0;
      this.each(function(){
        width = $(this)[0].scrollWidth;
        fixedWidth=parseInt( $(this).css('width') );
        maxWidth = parseInt( $(this).css('max-width') )||fixedWidth;
        if (width > maxWidth) {
          $(this).attr('title',$(this).text());
        }else{
          $(this).attr('title','');
        }
      })
    },

    /**
     *
     * @desc 监控输入框字段长度变化
     * @param {jQuery DOM} totalViewDom
     * @param {jQuery DOM} selfFieldChangeDom
     * @param {Number || maxlength} total
     */
    // monitFieldChange: function (totalViewDom, selfFieldChangeDom, total) {
    //   var obj = {};

    //   Object.defineProperty(obj, 'field', {
    //     set: function (v) {
    //       totalViewDom.text(v);
    //     }
    //   });

    //   obj.field = this[0].value.length;

    //   if (!this.attr('maxlength') && !total) {
    //     throw new Error(this.selector + ' 没有maxlength特性 或 没有第三个参数total（Number）');
    //   }

    //   this.on('focus input propertyChange', function (ev) {
    //     var number = Number($(this).attr('maxlength')) || total,
    //       len = ev.target.value.length;

    //     if (number - len >= 0) {
    //       obj.field = len;
    //       selfFieldChangeDom && selfFieldChangeDom.text(len);
    //     }
    //   });
    // },
    monitFieldChange: function (totalViewDom, selfFieldChangeDom, total) {
      var obj = {},self = this;
      Object.defineProperty(obj, 'field', {
        set: function (v) {
          // v = v > self.attr('maxlength') ? self.attr('maxlength') : v;
          totalViewDom.text(v);
        }
      });
      
      obj.field = this[0].value.length;
  
      if (!this.attr('maxlength') && !total) {
        throw new Error(this.selector + ' 没有maxlength特性 或 没有第三个参数total（Number）');
      }
      
      this.on('focus input propertyChange', function (ev) {
        var number = Number( $(this).attr('maxlength') ) || total,
            len = ev.target.value.length;
  
        if (number - len >= 0) {
          obj.field = len;
          selfFieldChangeDom && selfFieldChangeDom.text(len);
        }
        if(this.scrollHeight>this.clientHeight) {
          this.scrollTop = this.scrollHeight
        }
      });
    },

    setPlaceholder: function () {
      var isInputSupported = 'placeholder' in document.createElement('input');
      var textareaSupported = 'placeholder' in document.createElement('textarea');

      if (!isInputSupported) {
        this.filter('input').each(_each);
      }

      if (!textareaSupported) {
        this.filter('textarea').each(_each);
      }

      function _each(index) {
        var $this = $(this);
        var placeholder = $this.attr('placeholder');

        if (placeholder) {
          var isInput = $this.is('input');
          var position = $this.position();
          var borderLeftWidth = parseInt($this.css('border-left-width'));
          var paddingLeft = parseInt($this.css('padding-left'));
          var height = isInput ? ($this.outerHeight() + 'px') : 'auto';
          var $label = $('<label class="label__placeholder--text">' + placeholder + '</label>');

          $label.css({
            left: position.left + borderLeftWidth + paddingLeft,
            top: position.top,
            lineHeight: height,
            height: height
          });

          $this.parent().append($label);

          setTimeout(function () {
            // 防止有动态添加ID操作，需要异步
            var id = $this.attr('id');
            if (!id) {
              id = (isInput ? 'input' : 'textarea') + '__index--' + index;
              $this.attr('id', id);
            }
            $label.attr('for', id);
          }, 100);

          $this.on('blur', function () {
            this.value.length ? $label.css('display', 'none') : $label.css('display', '');
          });
        }
      }
    },

    hoverDir: function (mask, options) {
      var timer = null;
      var options = $.extend({
        speed: 300,
        easing: 'ease',
        hoverDelay: 0,
        inverse: false

      }, options);

      function _getStyle(dir) {
        var fromStyle, toStyle,
          slideFromTop = {
            left: '0px',
            top: '-100%',
            opacity: 0
          },
          slideFromBottom = {
            left: '0px',
            top: '100%',
            opacity: 0
          },
          slideFromLeft = {
            left: '-100%',
            top: '0px',
            opacity: 0
          },
          slideFromRight = {
            left: '100%',
            top: '0px',
            opacity: 0
          },
          slideTop = {
            top: '0px',
            opacity: 1
          },
          slideLeft = {
            left: '0px',
            opacity: 1
          };

        switch (dir) {
          case 0:
            // from top
            fromStyle = !options.inverse ? slideFromTop : slideFromBottom;
            toStyle = slideTop;
            break;
          case 1:
            // from right
            fromStyle = !options.inverse ? slideFromRight : slideFromLeft;
            toStyle = slideLeft;
            break;
          case 2:
            // from bottom
            fromStyle = !options.inverse ? slideFromBottom : slideFromTop;
            toStyle = slideTop;
            break;
          case 3:
            // from left
            fromStyle = !options.inverse ? slideFromLeft : slideFromRight;
            toStyle = slideLeft;
            break;
        };

        return {
          from: fromStyle,
          to: toStyle
        };
      }

      return this.on('mouseenter.hoverDir mouseleave.hoverDir', function (event) {
        var _this = $(this),
          $mask = _this.find(mask),
          dir = _this.mouseDirection(event),
          styles = _getStyle(dir);

        if (event.type === 'mouseenter') {
          $mask.hide().css(styles.from);

          clearTimeout(timer);
          _this.timer = setTimeout(function () {
            $mask.show(0, function () {
              $(this).stop().animate(styles.to, options.speed);
            });

          }, options.hoverDelay);

        } else {
          clearTimeout(timer);
          $mask.stop().animate(styles.from, options.speed);
        }
      });
    },

    hoverShow: function () {
      function _transOpacity(opacity) {
        return $(this).find('.mask-text').stop().animate({
          "opacity": opacity
        })
      }
      return this.hover(function (event) {
        _transOpacity.call(this, 1)
      }, function () {
        _transOpacity.call(this, 0)
      });
    },

    /**
     * 判断鼠标从哪个方向进入和离开容器
     * @param {Event Object} ev
     * @return {Number} 0, 1, 2, 3 分别对应着上，右，下，左
     */
    mouseDirection: function (ev) {
      var w = this.outerWidth(),
        h = this.outerHeight(),
        offset = this.offset(),
        x = (ev.pageX - offset.left - (w / 2)) * (w > h ? (h / w) : 1),
        y = (ev.pageY - offset.top - (h / 2)) * (h > w ? (w / h) : 1);

      return Math.round((((Math.atan2(y, x) * (180 / Math.PI)) + 180) / 90) + 3) % 4;
    },

    /**
     * @desc 倒计时
     * @param {Number} second 默认60
     */
    countDownTimer: (function () {
      var cDTimer = null;
      var html = undefined;

      return function __CDT(second) {
        cDTimer && clearTimeout(cDTimer);

        var self = this;
        var s = (second === undefined) ? 60 : second;

        if (html === undefined) {
          html = self.html();
        }

        if (s === 0) {
          return self.prop('disabled', false).html(html);

        } else {
          self.prop('disabled', true).html(s + '秒后重试');
          s--;
        }

        cDTimer = setTimeout(function () {
          __CDT.call(self, s);
        }, 1000);
      };
    }()),

    // 分页
    pageNav: function (pageInfo) {
      if (pageInfo != null && pageInfo.total > 0) {
        var html = '';
        //当前页
        var current = pageInfo.current;
        // 总页数
        var pages = pageInfo.pages;

        var orderBy = pageInfo.orderBy;
        // 如果不只有一页
        if (pages > 1) {
          if (current > 1) {
            var before = current - 1;
            //首页
            html += '<a class="item first" href="javascript:;" onclick="page.loadData(1, ' + orderBy + ')" title="首页"> << </a>';
            //上一页
            html += '<a class="item prev" href="javascript:;" onclick="page.loadData(' + before + ', ' + orderBy + ')" title="上一页"> < </a>';
          }
          //判断当前页的前页能不能往前查4页，除去自身应该是减五
          var beforeNum = current - 5 > 0 ? 5 : current;
          //判断当前页能不能往后数3页
          var endNum = current + 3 <= pages ? 3 : pages - current;

          var bef = 5 - beforeNum;
          var end = 3 - endNum;
          if (pages >= 8) {
            beforeNum = (current - 5 + end) > 0 ? (5 + end) : current;
          } else {
            beforeNum = current;
          }
          endNum = (current + 3 + bef) <= pages ? (3 + bef) : pages - current;

          //循环展示当前的前四页
          //计算出当前页往前数有几页超过四个就展示前四个，不超过就有几个展示几个
          for (var i = beforeNum - 1; i > 0; i--) {
            html += '<a class="item" href="javascript:;" onclick="page.loadData(' + (current - i) + ', ' + orderBy + ')">' + (current - i) + '</a>';
          }
          //当前页
          html += '<em class="item active">' + current + '</em>';
          //当前页往后数3页
          for (var i = 1; i <= endNum; i++) {
            html += '<a class="item" href="javascript:;" onclick="page.loadData(' + (current + i) + ', ' + orderBy + ')">' + (current + i) + '</a>';
          }
          if (pages > current) {
            var rear = current + 1;
            //下一页
            html += '<a class="item next" href="javascript:;" onclick="page.loadData(' + rear + ', ' + orderBy + ')" title="下一页"> > </a>';
            // 末页
            html += '<a class="item last" href="javascript:;" onclick="page.loadData(' + pages + ', ' + orderBy + ')" title="尾页"> >> </a>';
          }
        } else {
          html += '<em class="item active">1</em>';
        }
        this.html(html);
      }
    },
     // 自动提示邮箱后缀
  changeTips :function(value){
    value = $.extend({
      divTip:""
    },value)
    
    var $this = $(this);
    var indexLi = 0;
    var strHtml='<ul class="'+value.divTip.replace('.','')+'">\
                    <li data-email="@qq.com"></li>\
                    <li data-email="@163.com"></li>\
                    <li data-email="@123.com"></li>\
                    <li data-email="@sina.com"></li>\
                    <li data-email="@139.com"></li>\
                    <li data-email="@126.com"></li>\
                    <li data-email="@yahoo.com"></li>\
                    <li data-email="@gmail.com"></li>\
                </ul>';
    $this.parents('.ui-input').append(strHtml)
    //点击document隐藏下拉层
    $('body').on('click','#Mask',function(event){
      blus();
      $(this).remove();
    })
    $(value.divTip).on('mousedown','li',function(event){
      if(event.button!==0){
        return false
      }
      var liVal = $(event.target).text();
      value.invalid ? $this.val(liVal) : $this.val(liVal).valid();
      blus();
    })
    
    //隐藏下拉层
    function blus(){
      $(value.divTip).hide();
      $('#Mask').remove();
    }
    
    //键盘上下执行的函数
    function keychang(up){
      if(up == "up"){
        if(indexLi == 0){
          indexLi = $(value.divTip).find('li:not(:hidden)').length-1;
        }else{
          indexLi--;
        }
        
      }else{
        if(indexLi ==  $(value.divTip).find('li:not(:hidden)').length-1){
          indexLi = 0;
        }else{
          indexLi++;
        }
      }
      $(value.divTip).find('li:not(:hidden)').eq(indexLi).addClass("active").siblings().removeClass();
      var liVal = $(value.divTip).find('li:not(:hidden)').eq(indexLi).text();
      $this.val(liVal);
    }
    
    //值发生改变时
    function valChange(){
      var tex = $this.val();//输入框的值
      var fronts = "";//存放含有“@”之前的字符串
      var af = /@/;

      if(tex.charAt(0) == "@") {
        $this.val('');
        tex = '';
      }

      // var regMail = new RegExp(tex.substring(tex.indexOf("@")));//有“@”之后的字符串
     
      tex = tex.replace(/[\u4e00-\u9fa5]/g, '');

      //让提示层显示，并对里面的LI遍历
      if(tex==""){
        blus();
      }else{
        if(!$('#Mask').length) {
          $('body').prepend('<div id="Mask" style="position: absolute;left: 0;top: 0;z-index: 9;width: 100%;height: 100%;background: transparent;"></div>');
        }
        $(value.divTip).
        show().
        children().
        each(function(index) {
          var valAttr = $(this).data("email");
          if(index==0){$(this).addClass("active").siblings().removeClass();}
          //索引值大于1的LI元素进处处理
          // if(index>1){
            //当输入的值有“@”的时候
            if(af.test(tex)){
              var regMail = new RegExp(tex.substring(tex.indexOf("@")));//有“@”之后的字符串
              //如果含有“@”就截取输入框这个符号之前的字符串
              fronts = tex.substring(tex.indexOf("@"),0);
              $(this).text(fronts+valAttr);
              // 判断输入的值“@”之后的值，是否含有LI的email属性
              // if(!!regMail && regMail.test($(this).data("email"))){
              if(!!regMail && regMail.test($(this).data("email"))){
                $(this).show();

                // console.log($(value.divTip).find('li:not(:hidden)').length)
              }else{
                  $(this).hide();
              }
              //判断输入的值“@”之后的值，是否含有和LI的email属性
              // if(regMail.test($(this).data("email"))){
              //   $(this).show();
              // }else{
              //   if(index>1){
              //     $(this).hide();
              //   }	
              // }
              indexLi = 0;
              $(value.divTip).find('li:not(:hidden)').eq(indexLi).addClass("active").siblings().removeClass();
            }
            //当输入的值没有“@”的时候
            else{
              $(this).text(tex+valAttr).show();
            }
            if($(value.divTip).find('li:not(:hidden)').length===0) {
              $(value.divTip).hide();
            }
          // }
        })
      }	
    }

    $(this).bind("input",function(){
      valChange();
    })
    
    //鼠标点击和悬停LI
    $(value.divTip).children().
    hover(function(){
      indexLi = $(this).index();//获取当前鼠标悬停时的LI索引值;
      $(this).addClass("active").siblings().removeClass();
    })

    //按键盘的上下移动LI的背a景色
    $this.keydown(function(event){
      if(event.which == 38){//向上
        keychang("up")
      }else if(event.which == 40){//向下
        keychang()
      }else if(event.which == 13){ //回车
        var liVal = $(value.divTip).find('li:not(:hidden)').eq(indexLi).text();
        value.invalid ? $this.val(liVal) : $this.val(liVal).valid();
        blus();
      }
    })				
  }
  });

  /**
   *
   * @desc $ 序列化字符串，日期格式化
   * @example $.serialize().serializeDateFormat();
   * @returns {String} $ 序列化后的字符串，日期格式化
   */
  window.String.prototype['serializeDateFormat'] = function () {
    var str = this;
    var reg = /year=(\d{4})&month=(\d{1,2})&day=(\d{1,2})/i;
    var matchAry = str.match(reg);
    return str.replace(reg, 'date=' + $.dateDashJoin(matchAry[1], matchAry[2], matchAry[3], '-'));
  };

}(window, $, document));
;

// Placeholder 兼容
$('input,textarea').setPlaceholder();

(function ($) {
  // layer 全局设置
  layer.config({
    anim: 0, // 动画
    move: false, // 禁止移动
    shade: [0, 'transparent'], // 遮罩层透明掉
    shadeClose: true, // 点击遮罩层关闭弹框
    // success: function (layero, index) {
    //   console.log(layero, index);
    // },
    // yes: function (index, layero) {
    //   console.log(layero, index);
    // },
    // cancel: function (index, layero) {
    //   console.log(layero, index);
    // },
    // end: function () {
    // }
  });


  var slice = Array.prototype.slice;
  // layer callbacks
  var layerCallbacks = ['success', 'yes', 'cancel', 'end'];

  /**
   * @dec 构造函数
   * @param {Object} params layer参数，参数中请不要写 上面数组的回调，被重写了
   */
  function Modal(params) {
    this.callbacks = {};
    this.params = params;

    /**
     * resolve(this) 把 this传到 then回调函数
     */
    Promise.resolve(this).then(function (self) {
      self.index = layer.open($.extend(self.params, self._callbackMount()));
    });
  }

  // 回调函数，改成链式调用
  layerCallbacks.forEach(function (method) {
    Modal.prototype[method] = function (fn) {
      if ( !this.callbacks[method] ) {
        this.callbacks[method] = [];
      }

      if (this.callbacks[method].length >= 4) {
        throw method + ' 回调限制在4个';
      }
      
      this.callbacks[method].push(fn);
      return this;
    };
  });

  Modal.prototype._callbackMount = function () {
    var self = this;
    var result = {};

    layerCallbacks.forEach(function (fn) {
      result[fn] = function () {
        if ( !!self.callbacks[fn] ) {
          var args = slice.call(arguments);

          if (fn === 'success') {
            var index = args[1];
            args[1] = args[0];
            args[0] = index;
          }

          // iframe 框，获取 iframe的相关对象
          if (self.params.type === 2 && fn !== 'end') {
            this.$iframeElement = args[1].find('iframe');
            this.iframeElement = this.$iframeElement[0];
            this.iframeWindow = window[this.iframeElement['name']];
          }

          var fns = self.callbacks[fn];
          for (var i = 0, len = fns.length; i < len; i++) {
            var isClose = fns[i].apply(this, args);

            if ('end' === fn || isClose) {
              self.destroy();
            }
          }
        }
      };
    });
    return result;
  }

  Modal.prototype.destroy = function () {
    this.callbacks = {};
    layer.close(this.index);
  };

  //============ Modal 弹框 ==============================================
  $.extend({
    /**
     * this是 jQuery
     * @param {Object} params => layer params
     */
    confirmModal: function (params) {
      params = params || {};
      return new Modal(this.extend({
        type: 0,
        title: '提示',
        skin: 'confirmModal-tips',
        area: '360px',
        btn: ['保存', '取消'],
        content: '一个账号只能选择一个身份，是否要保存为企业信息？'

      }, params));
    },

    /**
     *
     * @param {String} type [success, error]
     * @param {Object} params  => layer params
     */
    alertModal: function (type, params) {
      type = type || 'success';
      params = params || {};

      if (!params.content) {
        var content = '<div class="auto-wrap clearfix">\
                          <i class="iconfont icon-'+ type +'"></i>\
                          <P>'+(params.text || '')+'</P>\
                      </div>';
      }

      return new Modal(this.extend({
        type: 1,
        time: 3500,
        title: '温馨提示',
        shade: [.1, '#000'],
        skin: 'alertModal-tips',
        id: 'alertModal_o_alertModal', // 禁止重复弹出
        area: ['370px', undefined],
        content: content

      }, params));
    },

    /**
     * @param {Object} params => layer params
     */
    iframeModal: function (params) {
      params = params || {};
      return new Modal(this.extend({
        type: 2, // iframe层
        title: '修改手机号码',
        area: ['428px', '294px'],
        content: ''

      }, params));
    },

    uploadPhoto: function (options) {
      options = options || {}
      var title =options.title||'<div class="form-group">\
        <label group="label" class="not-required">上传到:</label>\
        <div group="control">\
        <div class="select2-overlay">\
        <select disabled name="photo-select2">'+(options.select2 || '')+'</select>\
        </div>\
        </div></div>';

      return $.confirmModal({
        type: 1,
        id: 'uploadPhotosdsad',
        title: title,
        skin: 'layer-photo-upload',
        content: options.content || '',
        area: ['820px', '450px'],
        btn: false,
        shadeClose: false
      }).success(function (index, layero) {
        layero.parent().removeClass('hide');
        // 渲染 select2
        layero.find('.select2-overlay').reloadSelect2();
        
      }).end(function () {
        options.up.clearCount();
      });
    },

    multiSelectPopout: function (params) {
      var html = '';
      var id = '#' + params.id;
      var sign = params.sign || ',';
      var data = params.data || {};
      var hasKey = params.element.val();
      // console.log(data);
      for (var key in data) {
        if ( data.hasOwnProperty(key) ) {
          html += '<label class="ui-checkbox popout-change__select">\
          <input value="'+ data[key]['id'] +'" type="checkbox"'+ (hasKey.indexOf(sign + data[key]['id']) !== -1 ? ' checked' : '') +'>\
          <em>'+ data[key]['area'] +'</em></label>';
        }
      }
  
      // 地区选择并弹框中的change事件
      $(document).on('change.popoutChange', '.popout-change__select input', function (event) {
        event.preventDefault();
        var target = $(id).find('input'),
            value = target.val().replace('on', '');
  
        $(this).parents('.layui-layer-content').find('input:checked').length
          ? target.prop('checked', true)
          : target.prop('checked', false);
  
        this.checked
          ? target.val( value + sign + this.value )
          : target.val( value.replace(sign + this.value, '') );
          $('[data-checkbox="select"]').find('input').valid();
        return false;
      });

      // return $.confirmModal({
      //   id: 'popout_' + params.id,
      //   title: false,
      //   type: 4,
      //   shadeClose:true,
      //   shade: [0.3, 'transparent'],
      //   skin: 'multi-select-popout',
      //   tips: [3, '#ecffff'],
      //   closeBtn: false,
      //   time: 0,
      //   resize: false,
      //   fix: false,
      //   area: 'auto',
      //   btn: false,
      //   maxWidth: 470,
      //   content: ['<div class="clearfix">' + html + '</div>', id]
      // }).end(function () {
      //   $(document).off('change.popoutChange');
      // });
      layer.tips('<div class="clearfix">' + html + '</div>', $(id).find('em')[0], {
        tips: [3,'#5ab1e1'],
        shade:[0.1],
        maxWidth: 470,
        skin: 'multi-select-popout',
        shadeClose:true,
        time: 0,
        end:function(){
          $(document).off('change.popoutChange');
        }
      })
    },

    selectUploadWay: function(list, options) {
      options = options || {};
      if (options.$view && options.$view.attr('readonly')) {
        return;
      }

      list = $.extend([
        {
          href: 'javascript:;',
          icon: 'icon-xiangce',
          text: options.type === 'videos' ? '我的视频' : '我的相册'
        },
        {
          href: 'javascript:;',
          icon: 'icon-bendisc',
          text: '本地上传'
        }
      ], list || []);

      var content = '<div class="upload-way-box clearfix">';
      list.forEach(function (item, ind) {
        content += '<a href="'+ item.href +'" class="item-'+ ind +'"><i class="reds iconfont '+ item.icon +'"></i><em>'+ item.text +'</em></a>';
      });
      content += '</div>';

      return $.confirmModal({
          title: '请选择上传方式',
          content: content,
          skin: 'upload-way-iframe',
          area: ['576px', '270px'],
          btn: false
        }).success(function (index, layero) {
          var $item = layero.find('.upload-way-box a');

          var up = options.$view.getDirect();
          up.setOption($.extend(options, {
            target: $item.eq(1).get(0),
            container: layero.find('.layui-layer-content').get(0),
            layerIndex: index
          }));

          $item.on('click', function () {
            layer.close(index);
            if($(this).hasClass('item-0')){
              $.tabAlert({}, options.url, options.url1, up);//up.options.max-up._count
            }
          });

          // 非 object标签
          if (document.documentMode || navigator.userAgent.indexOf('Edge') > -1) {
            // 如果是 SB 浏览器
            layero.find('input[type=file],.upload-input').on('click change', function () {
              // 因为input[type=file]在layer弹框，关闭了弹框那input就移除了
              // layer.close(index);
              layero.css('opacity', '0');
            });
          }
        }).end(function () {
          var up = options.$view.getDirect();
          up.destroy();
          return true;
        });
    },

    tabAlert: function(parameters, url, url1, up){
      var number = 1;

      var defaultParameters={
        area: ['940px', '854px'],
        skin: 'photos-alert',
        fix: false,
        btn: ['确定','取消'],
        tab: [{
          title: '全部' + (up.options.type === 'videos' ? '视频' : '照片'),
          content: '<iframe name="allPhoto" src="'+ (url || '/html/iframe_layer/all_photo.html') +'" frameborder="0" width="100%" height="766px"></iframe>'
        }, {
          title: '我的' + (up.options.type === 'videos' ? '视频' : '相册'),
          content: '<iframe name="myPhoto" src="'+ (url1 || '/html/iframe_layer/my_photo.html') +'" frameborder="0" width="100%" height="766px"></iframe>'
        }],
        success: function(layero,index){
          if (up.options.multiple) {
            number = up.options.max - up.count$;
          }

          layero.find('.layui-layer-title')[0].data={};
          layero.find('.layui-layer-title').attr('number',number).on('click','span',function(){
            var $this=$(this);

            if($this.hasClass('layui-layer-tabnow')){return};

            var prevIndex=layero.find('.xubox_tab_layer').index();//获取上个显示的iframe的document
            var $prevDocument=$(window[layero.find('iframe')[prevIndex]['name']].document);
            
            //切换iframe
            $this.addClass('layui-layer-tabnow').siblings('span').removeClass('layui-layer-tabnow');
            layero.find('.layui-layer-tabli').removeClass('xubox_tab_layer').eq($(this).index()).addClass('xubox_tab_layer')
            
            var currentIndex=layero.find('.xubox_tab_layer').index();//获取当前显示的iframe的document
            var $currentDocument=$(window[layero.find('iframe')[currentIndex]['name']].document);
            
            $prevDocument.find('input[type=checkbox]:checked').prop('checked',false);
            
            $currentDocument.find('img').each(function(){
              if($this.parents('.layui-layer-title')[0].data[$(this).attr('src')]){
                $(this).parents('li').find('input[type=checkbox]').prop('checked',true)
              }
            })
            window[layero.find('iframe')[currentIndex]['name']].showOrNo&&window[layero.find('iframe')[currentIndex]['name']].showOrNo()
          })
        },
        yes: function(index, layero){
          var data = layero.find('.layui-layer-title')[0].data;
          for (var key in data) {
            if (data.hasOwnProperty(key)) {
              up.preRenderLoading(key);    
              up.render(key);  
            }
          }
          layer.close(index);
        },
        btn2: function(index,layero){
          var index=layero.find('.xubox_tab_layer').index();
          var $currentDocument=$(window[layero.find('iframe')[index]['name']].document);
          $currentDocument.find('input[type=checkbox]:checked').prop('checked',false);
          layero.find('.layui-layer-title')[0].data={};
          layero.find('.layui-layer-btn').removeClass('show').siblings('.layui-layer-setwin').removeClass('hide');
          return false
        },
        end: function(index,layero){
          
        }
      };
      var newParameters=$.extend({},defaultParameters,parameters);
      layer.tab(newParameters);
    }
  });

}(jQuery));
;
(function ($, window, document, undefined) {
  var typeIsAlert = '@alert@';
  var typeIsDom = '$selector$';

  // 设置全局默认
  $.validator.setDefaults({
    onkeyup: null,
    errorElement: 'span',
    errorClass: 'help-block',
    ignore: '.form-ignore',
    errorPlacement: function (error, inputCtrl) {
      error.addClass('has-error help-block');
      var customType = this.settings.customType;

      switch (customType) {
        //================ 弹框提示验证 ================
        case typeIsAlert:
          var errorMap = this.errorMap;
          for (var key in errorMap ) {
            if ( errorMap.hasOwnProperty(key) ) {
              Promise.resolve(null).then(function () {
                var errElement = $('.help-block[name="'+ key +'"]').eq(0);
                errElement.length && setIntoView(errElement) && errElement.focus();

                $.alertModal('error', {
                  time: 3500,
                  text: errorMap[key]

                }).end(function () {
                  errElement.focus();
                });
              });
              // 每次只提示第一个
              return false;
            }
          }
          break;

        //================ 指定某个元素作为容器，如登录注册页面 ================
        case typeIsDom:
          var errSelector = this.settings.showErrorSelector;
          var $showError = $(errSelector);
          var errorList = this.errorList;

          if (errorList.length) {
            var element = errorList[0].element;
            // username-error,password-name
            var id = (element.name || '') + '-error';
            var name = (element.id || '') + '-error';
            var attr = error.attr('id');
            element.focus();
            
            if (name === attr || id === attr) {
              // 需要清空
              $showError.html('').append(error);
            }
            
            if ($showError.find('.help-block.has-error').length) {
              // 停止下一步验证
              return false;
            }
          }
          break;

        //================ 默认全部验证 ================
        default:
          var $formGroup = inputCtrl.parents('.form-group');
          var key = inputCtrl.data('key'); 
           // 获取对应错误控件的错误信息容器，用于存放 error
          var $errorGroup = $('[data-help-block="'+ key +'"]').eq(0);
          var errorList = this.errorList;

          if (!$formGroup.find('.help-block.has-error').length) {
            if ($errorGroup.length) {
              $errorGroup.append(error);
            }else {
              $formGroup.append(error);
            }
          }

          var $firstError = this.toShow.eq(0);
          // 定位到错误的第一个
          $firstError.length && setIntoView($firstError);
          if (errorList.length && errorList[0].element.tagName !== 'SELECT') {
            errorList[0].element.focus();
            return
          }
          break;
      }
    },
    success: function (error, inputCtrl) {
      error.remove();
    }
  });

  // ==========================
  var methodColl = {
    get: function (name) {
      return this[name].regTest;
    },
    isWordOrNum: {
      errorMsg: '请输入数字或汉字',
      regTest: function (value) {
        return /^[\u4e00-\u9fa50-9]*$/g.test(value);
      }
    },
    isTrim: {
      errorMsg: '输入内容不能全是空格',
      regTest: function (value) {
        return value.length && value.trim() !== '';
      }
    },
    isPhone: {
      errorMsg: '手机号码格式错误',
      regTest: function (value) {
        // /(^1[3|4|5|6|7|8|9][0-9]{9}$)/
        return /(^1[3-9][0-9]{9}$)/.test(value);
      }
    },
    isEmail: {
      errorMsg: '邮箱格式错误',
      regTest: function (value) {
        // /^[\w.\-]+@(?:[a-z0-9]+(?:-[a-z0-9]+)*\.)+[a-z]{2,3}$/
        // /^\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}$/
        return /^(\w-*\.*)+@(\w-?)+(\.\w{2,})+$/.test(value);
      }
    },
    isUserName: {
      errorMsg: '4-12位中文、数字和字母组合，区分大小写',
      regTest: function (value) {
        // (?![a-zA-Z]+$)
        return /^(?![0-9]+$)[0-9A-Za-z\u4e00-\u9fa5]{4,12}$/.test(value);
      }
    },
    isPassword: {
      errorMsg: '密码格式6-12位字母或数字组合，区分大小写',
      regTest: function (value) {
        return /[a-zA-Z0-9]{6,12}$/.test(value);
      }
    },
    note_code: {
      errorMsg: '短信验证码格式错误',
      regTest: function (value) {
        return /[0-9]{4,6}/.test(value);
      }
    },
    isTel: {
      errorMsg: '电话号码格式错误',
      regTest: function (value) {
        // /(^0(10|2[0-5789]|\\d{3})\\d{7,8}$)/
        // /^(\d{3,4}-?)?\d{7,9}$/
        // /^(\d{3,4}-)?\d{7,8}$/
        return /^[^\u4e00-\u9fa5A-Za-z]{6,64}$/.test(value) || this.get('isPhone')(value);
      }
    },
    isPhoneOrEmail: {
      errorMsg: '手机号码或邮箱格式错误',
      regTest: function (value) {
        return this.get('isPhone')(value) || this.get('isEmail')(value);
      }
    },
    isPhoneOrUserName: {
      errorMsg: '手机或用户名格式错误',
      regTest: function (value) {
        return this.get('isPhone')(value) || this.get('isUserName')(value);
      }
    },
    isQQ: {
      errorMsg: 'QQ格式错误',
      regTest: function (value) {
        return /^[1-9]\d{4,15}$/.test(value) || this.get('isPhone')(value) || this.get('isEmail')(value);
      }
    },
    isWeChat: {
      errorMsg: '微信号格式不正确',
      regTest: function (value) {
        return /^[a-zA-Z0-9]{1}[-_a-zA-Z0-9]{5,19}$/.test(value) || this.get('isPhone')(value);
      }
    },
    isIdCard: {
      errorMsg: '身份证号码格式错误',
      regTest: function (value) {
        return /^(\d{6})()?(\d{4})(\d{2})(\d{2})(\d{3})(\w)$/.test(value);
      }
    },
    isZh: {
      errorMsg: '请输入中文',
      regTest: function (value) {
        return /^[\u4e00-\u9fa5]+$/.test(value);
      }
    },
    isUrl: {
      errorMsg: '网址格式错误',
      regTest: function (value) {
        return /^((http|ftp|https):\/\/)?[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&:/~\+#]*[\w\-\@?^=%&/~\+#])?$/.test(value);
      }
    },
    isPostCode: {
      errorMsg: '邮编格式错误',
      regTest: function (value) {
        return /^[0-9]{6,10}$/.test(value);
      }
    },
    floatNumber: {
      errorMsg: $.validator.format('请保留小数点后{0}位'),
      regTest: function (value, params) {
        var str = param && (param + '').indexOf(',') < 0 ? '^\\d+(\\.\\d{1,' + param + '})?$' : '^\\d+(\\.\\d{' + param + '})?$'
        var reg = new RegExp(str);
        return reg.test(value);
      }
    },
    gt: {
      errorMsg: $.validator.format('请输入大于{0}的数字'),
      regTest: function (value, params) {
        return value > params;
      }
    },
    lt: {
      errorMsg: $.validator.format('请输入小于{0}的数字'),
      regTest: function (value, params) {
        return value < params;
      }
    },
    eqText: {
      errorMsg: $.validator.format('请输入{0}'),
      regTest: function (value, params) {
        return value === params;
      }
    }
  };

  function validatorAddMethod(object) {
    for (var methodName in object) {
      if ( object.hasOwnProperty(methodName) && methodName !== 'get' ) {
        _addExecContext(methodName);
      }
    }

    function _addExecContext(methodName) {
      var item = object[methodName];

      $.validator.addMethod(
        methodName,
        function (value, element, params) {
          return this.optional(element) || item.regTest.bind(methodColl)(value, params);
        },
        item.errorMsg
      );
    }
  }

  validatorAddMethod(methodColl);


  function getWinScrollTop() {
    return $(window).scrollTop() || $('body').scrollTop();
  }

  function setIntoView($obj, d) {
    if ($.isInIframe()) {
      return true;
    }

    d = d || 280;
    var winTop = getWinScrollTop() + 90;
    var top = $obj.offset().top;

    if (top <= winTop && top > 0) {
      $obj.scrollIntoView();
      var _winTop = getWinScrollTop();
      $(window).scrollTop(_winTop - d);
    }
    return true;
  }

  /**
   *
   * @desc 弹框式提示 或者 指定某个容器存放错误信息
   * 既一步一步验证
   */
  function diffValidate(type, selector) {
    var config = {
      customType: type,
      onfocusout: false,
      showErrorSelector: selector,
      showErrors: function (errorMap, errorList) {
        this.defaultShowErrors();
      }
    };

    // if (type === typeIsAlert) {
    //   config.onfocusout = false;
    // }

    $.validator.setDefaults(config);
  }

  /**
   * @desc validate封装
   * @param {Object} config
   * @returns {Object} validator
   */
  $.fn.customValidator = function (config, type) {
    if (!this.is('form')) {
      throw new Error(this.selector + ' 不是一个form元素');
    }

    var validObject = loopAddValidate.apply(this, [{
      rules: {},
      messages: {}

    }, config]);

    var __defaultParams = $.extend({}, validObject);
    if (type !== undefined) {
      var selector = type;
      type = typeIsDom;
    }

    if (this.data('diff') === 'form') {
      type = typeIsAlert;
    }

    if (type) {
      diffValidate(type, selector);
    }

    console.log(__defaultParams);
    return $.fn.validate.call(this, __defaultParams);
  };

  /**
   *
   * @param {Object} validObject
   * @param {Object} config
   * @returns {Object}
   */
  function loopAddValidate(validObject, config) {
    config = config || {};

    var rules = {},
        messages = {};

    this.find('[name][data-key],.ueditor.ui-input').each(function () {
      var $this = $(this);
      var $label = $this.parents('.form-group').find('> label:first-child');
      var isRequired = $label.hasClass('not-required');
      // debugger
      var type = ($this.attr('type') || '').toLowerCase();
      var name = $this.attr('name');
      var key = $this.data('key');
      var text = $label.text().replace(/\s*/g, '');
      var prefix = '请输入';

      if (type && 'button|file'.indexOf(type) >= 0) {
        return;
      }

      if ((type && 'radio|checkbox'.indexOf(type) >= 0) || this.tagName === 'SELECT') {
        prefix = '请选择';
      }

      if ($this.data('upload') !== undefined) {
        prefix = '请上传';

        if (text.indexOf('上传') > -1) {
          prefix = '请';
        }
      }

      if (!!name && !!key) {
        rules = setRulesAndMsges(validObject.rules, name, !isRequired);
        messages = setRulesAndMsges(validObject.messages, name, (!isRequired ? (prefix + text) : ''));

        if ('radio|checkbox'.indexOf(type) < 0 || this.tagName === 'TEXTAREA') {
          rules.isTrim = true;
        }

        if (key !== 'get' && methodColl.hasOwnProperty(key)) {
          rules[key] = true;
        }

        if (config.rules) {
          validObject.rules[name] = $.extend(rules, config.rules[key] || config.rules[name]);
        }

        if (config.messages) {
          validObject.messages[name] = $.extend(messages, config.messages[key] || config.messages[name]);
        }
      }

      if (key && !name) {
        throw new Error(key + ' 对应的input name必须写');
      }
    });


    return validObject;
  }

  /**
   *
   * @param {Object} rulesMsges
   * @param {String} key
   * @param {Boolean and String} isRequired
   * @returns {Object}
   */
  function setRulesAndMsges(rulesMsges, key, isRequired) {
    return rulesMsges[key] = {
      required: isRequired
    };
  }


  /**
   * =============================================================================
   * ============== @desc 以下是禁止输入类型，及光标处理 ==============
   * =============================================================================
   */
  // 限制输入类型
  var dictMatch = {
    'number': /\D/g, // 只能输入数字
    'numberAbc': /[^A-Za-z0-9]/g, // 只能输入数字和字母
    'abc': /[^A-Za-z]/g, // 只能输入字母
    '!zh': /[\u4e00-\u9fa5\s]/g // 不能输入中文
  };

  function amount(that) {
    var regStrs = [
      ['^0(\\d+)$', '$1'], // 禁止录入整数部分两位以上，但首位为0
      ['[^\\d\\.]+$', ''], // 禁止录入任何非数字和点
      ['^\\.', ''], // 不能以小数点开头
      ['\\.(\\d?)\\.+', '.$1'], // 禁止录入两个以上的点
      ['^(\\d+\\.\\d{2}).+', '$1'] // 禁止录入小数点后两位以上
    ];

    for (var i = 0; i < regStrs.length; i++) {
      var reg = new RegExp(regStrs[i][0]);
      that.value = that.value.replace(reg, regStrs[i][1]);
    }
  }

  var matchFn = function () {
    var dict = $(this).data('dict');
    var value = this.value;

    if (dict === 'decimal') {
      return amount(this);
    }

    if (dict && dictMatch.hasOwnProperty(dict) && !!value.match(dictMatch[dict])) {
      this.value = value.replace(dictMatch[dict], '');
      $.setRange(this, +this.getAttribute('data-range'));
    }
  };

  var cpLock = true;
  var startValue;

  $('[data-dict]').off().on({
    'click.dict keyup.dict': function (e) {
      $.updateRange(this);
    },
    'compositionstart.dict': function () {
      // 中文输入开始
      // matchFn.call(this);
      $.updateRange(this, 'zh-start');
      startValue = this.value;
      cpLock = false;
    },
    'compositionend.dict': function () {
      // 中文输入结束
      matchFn.call(this);

      var zhStart = +this.getAttribute('zh-start');
      startValue === this.value && $.setRange(this, zhStart);

      cpLock = true;
    },
    'input.dict propertychange.dict': function () {
      // input框中的值发生变化
      cpLock && matchFn.call(this);
    }
  });

})(jQuery, window, document);
;
(function () {
  var cateRight = $('.ui-row > .ui-col-sm-11 > .cate-items');
  var maxWidth = parseInt( cateRight.css('max-width') );

  cateRight.each(function (cindex) {
    var $this = $(this);
    var totalWidth = 0;
    var cell = $this.find('.cate-cell');
    var spread = $this.next('.cate-spread');
    var stop;

    cell.slice(0, 28).each(function () {
      var self = $(this);
      // var margin_right = parseInt( self.css('margin-right') );
      // totalWidth += (self.outerWidth() + margin_right);

      if (totalWidth < maxWidth) {
        var margin_right = parseInt( self.css('margin-right') );
        totalWidth += (self.outerWidth() + margin_right);
        stop = self.index();
      }else {
        stop = self.index() - 1;
        return false;
      }
    });

    var index = $this.find('a.active').index() + 1;

    if (totalWidth >= maxWidth) {
      $this.find('a').slice(stop, stop + 1).attr('data-stop', stop);

      if ( stop && index > stop ) {
        $this.css('height', 'auto');
        !spread.length && $this.parent().append('<div class="cate-spread pull-right sideup"><em class="boult up_b">收起</em></div>');

      }else {
        !spread.length && $this.parent().append('<div class="cate-spread pull-right"><em class="boult">更多</em></div>');
      }
    }

  });

  function hrefParams($elem, itemsIndex, href, one) {
    var href = href || $elem.attr('href');
    var actived = $elem.parent().find('a.active').get(0);

    if (href.indexOf('item=') < 0 ) {
      href = href + (href.indexOf('?') > -1 ? '&item=' : '?item=');

      $elem.parents('.cate-layout').find('a.active').each(function () {
        var stop = $(this).parent().find('a[data-stop]').data('stop');
        var parentIndex = actived === this ? '' : cateRight.index( this.parentNode );
        var activeIndex = $(this).index() + 1;

        if (stop !== undefined && activeIndex > stop && itemsIndex.indexOf(parentIndex) < 0 && !one) {
          itemsIndex += '-' + parentIndex;
        }
      });

      itemsIndex = itemsIndex.replace(/^-|-$/g, '');
      return href + itemsIndex;
    }
    return '';
  }

  // 传参给后台，
  cateRight.find('a').on('click', function () {
    var $this = $(this);
    var index = $this.index();
    var stop = $this.parent().find('a[data-stop]').data('stop');
    var itemsIndex = stop === undefined || index < stop ? '' : ('' + cateRight.index( this.parentNode ));

    var ret = hrefParams($this, itemsIndex );
    ret && (this.href = ret);
  });

  $('body').on('click.itemsIndex', '#marblesDropdown .marble-bd a', function () {
    var $li = $(this).parent('li');
    var index = $li.index();
    var pIndex = $li.parents('.marble').index() + 1;
    var $cate = cateRight.eq(pIndex).find('a').eq(index);

    var stop = $cate.parent().find('a[data-stop]').data('stop');
    var itemsIndex = stop === undefined || index < stop ? '' : ('' + pIndex);
    var ret = hrefParams($cate, itemsIndex, this.href, true);

    ret && (this.href = ret);
  });


  $('.cate-spread').on('click', function () {
    if ( $(this).hasClass('sideup') ) {
      $(this).removeClass('sideup').html('<em class="boult">更多</em>').prev('.cate-items').css('height', '');

    }else {
      $(this).addClass('sideup').html('<em class="boult up_b">收起</em>').prev('.cate-items').css('height', 'auto');
    }
  });
})();
;

(function ($, window, document, undefined) {
  var $swiper = $('.swiper-container');

if ( $swiper.length && !$swiper.hasClass('not-swiper') ) {
  var fragment = document.createDocumentFragment();
  var $slide = $swiper.find('.swiper-slide');
  var $pagination = $swiper.find('.swiper-pagination');
  // 默认显示第一张
  var start = 0;

  $slide.each(function (index) {
    var $bullet = $('<span class="swiper-pagination-bullet"></span>');
    if (index === start) {
      $bullet.addClass('swiper-pagination-bullet-active');
    }
    fragment.appendChild($bullet[0]);
  });

  $pagination.length
    ? $pagination.append(fragment)
    : $('<div class="swiper-pagination"></div>').append(fragment).appendTo($swiper);

  if ($slide.length) {
    var pw = new pageSwitch($swiper.find('.swiper-wrapper')[0], {
      duration: 600,
      start: start,
      // 横向 mousemove
      direction: 0,
      loop: true,
      ease: 'ease-out',
      // transition: 'scrollCoverOutX',
      transition: 'scrollX',
      // freeze: false,
      mouse: true,
      autoplay: true,
      mousewheel: false,
      arrowkey: false,
      interval: 3500
    });

    var $bullets = $swiper.find('.swiper-pagination-bullet');

    pw.on('before', function(m, n){
      $bullets.eq(m).removeClass('swiper-pagination-bullet-active');
      $bullets.eq(n).addClass('swiper-pagination-bullet-active');
    });

    $bullets.on('click', function () {
      var index = $(this).index();
      pw.slide(index);
    });

    $swiper.on('mouseover', function () {
      pw.pause();
    }).on('mouseout', function () {
      pw.play();
    });
  }
}
;
  $(document.body).on('mouseenter', '.navbar-dropdown.magnetic-dropdown > dd', function () {
  var id = $(this).data('switch');
  $(this).find('a').addClass('dropdown-active').parent().siblings().find('a').removeClass('dropdown-active');
  $('#marblesDropdown').find('.marbles-li').addClass('hide').end().find('[data-case="' + id + '"]').removeClass('hide');

}).on('mouseleave', '.navbar-dropdown.magnetic-dropdown > dd', function (event) {
  var dir = $(this).mouseDirection(event);
  var isHide = $(this).parent('.magnetic-dropdown').css('display');

  if (dir !== 1 || isHide === 'none') {
    $('#marblesDropdown').find('.marbles-li').addClass('hide');
    $(this).find('a').removeClass('dropdown-active');
  }

}).on('mouseleave', '#marblesDropdown', function () {
  $(this).prev('dl').find('a').removeClass('dropdown-active');
  $(this).find('.marbles-li').addClass('hide');
});
;

  // 以下是地区选择
  $('.ui-checkbox').on('click', '.multi-select__boult', function (event) {
    event.preventDefault();
    var parent = $(this).parents('.ui-checkbox');
    parent.attr('id', 'boult_' + parent.index());
  });

  // 本来想用 trigger 触发，但是 trigger会选中
  $('input[data-clouds=country]').each(function(){
    if ($(this).is(':checked')) {
      __cloudsDisabled($(this).parent().parent().find('input[data-clouds=city]'), true, 'removeClass', 'addClass', false);
    }
  });

  function __cloudsDisabled(city, isDisabled, fn1, fn2, setChecked) {
    if (typeof setChecked === 'boolean') {
      city.prop('checked', setChecked);
      if (setChecked === false) {
        city.each(function (i) {
          $(this).val( this.value.split(',')[0] );
        });
      }
    }

    city.prop('disabled', isDisabled).next('.boult')[fn1]('multi-select__boult');
    city.parent('.ui-checkbox')[fn2]('clouds-disabled');
  }

  $('input[data-clouds]').on('click.clouds', function () {
    var that = $(this);
    var clouds = that.data('clouds'); // country(全国) | city(城市) | foreign(海外)

    if (clouds === 'country') {
      var city = that.parent().parent().find('input[data-clouds=city]');
      this.checked 
        ? __cloudsDisabled(city, true, 'removeClass', 'addClass', false) 
        : __cloudsDisabled(city, false, 'addClass', 'removeClass');
    }

    if (clouds === 'city' || clouds === 'foreign') {
      var values = this.value.split(',');
      if (!this.checked && values.length > 1) {
        $(this).val( values[0] );
      }
    }
  });
  //回到顶部按钮
  $.goTop();

}(jQuery, window, document));



<script type="text/javascript">
    /**
     * 修改“当前位置”
     * @param menus，格式如：[{name:"name1"}, {name:"name2",url:"http://www.seeyon.com"}]
     */
    function updateCurrentLocation(menus) {
        var icon = getCtpTop().currentSpaceType || "personal";
        var skinPathKey = getCtpTop().skinPathKey || "defaultV51";
        var html = "<span class='nowLocation_ico'><img src='" + _ctxPath + "/main/skin/frame/" + skinPathKey + "/menuIcon/" + icon + ".png'/></span>";
        html += "<span class='nowLocation_content'>";
        var items = [];
        for (var i = 0; i < menus.length; i++) {
            if (menus[i].url) {
                items.push("<a class='hand' onclick=\"showMenu('" + _ctxPath + menus[i].url + "')\">" + menus[i].name + "</a>");
            } else {
                items.push('<a>' + escapeStringToHTML(menus[i].name, false) + '</a>');
            }
        }
        html += items.join(' > ');
        html += '</span>';
        getA8Top().showLocation(html);
    }
</script>
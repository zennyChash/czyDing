Ext.namespace("Ext.ux.plugins");

Ext.ux.plugins.GroupHeaderGrid = function(config) {
	Ext.apply(this, config);
};

Ext.extend(Ext.ux.plugins.GroupHeaderGrid, Ext.util.Observable, {
	init: function(grid) {
		var v = grid.getView();
		v.beforeMethod('initTemplates', this.initTemplates);
		v.renderHeaders = this.renderHeaders.createDelegate(v, [v.renderHeaders]);
        v.afterMethod('onColumnWidthUpdated', this.updateGroupStyles);
        v.afterMethod('onAllColumnWidthsUpdated', this.updateGroupStyles);
		v.afterMethod('onColumnHiddenUpdated', this.updateGroupStyles);
		v.getHeaderCell = this.getHeaderCell;
		v.updateSortIcon = this.updateSortIcon;
		v.getGroupStyle = this.getGroupStyle;
	},

	initTemplates: function() {
		var ts = this.templates || {};
		if (!ts.gcell) {
			ts.gcell = new Ext.Template(
				'<td class="x-grid3-hd {cls} x-grid3-td-{id}" style="{style}">',
				'<div {tooltip} class="x-grid3-hd-inner x-grid3-hd-{id}" unselectable="on" style="{istyle}">{value}</div>',
				'</td>'
			);
		}
		this.templates = ts;
	},

	renderHeaders: function(renderHeaders) {
		var ts = this.templates, rows = [], tw = this.getTotalWidth();
		if(this.cm.rows){
			for (var i = 0; i < this.cm.rows.length; i++) {
				var r = this.cm.rows[i], cells = [], col = 0;
				for (var j = 0; j < r.length; j++) {
					var c = r[j];
					c.colspan = c.colspan || 1;
					c.col = col;
					col += c.colspan;
					var gs = this.getGroupStyle(c);
					cells[j] = ts.gcell.apply({
						id: c.id || i + '-' + col,
						cls: c.header ? 'ux-grid-hd-group-cell' : 'ux-grid-hd-nogroup-cell',
						style: 'width:' + gs.width + ';' + (gs.hidden ? 'display:none;' : '') + (c.align ? 'text-align:' + c.align + ';' : ''),
						tooltip: c.tooltip ? (Ext.QuickTips.isEnabled() ? 'ext:qtip' : 'title') + '="' + c.tooltip + '"' : '',
						value: c.header || '&#160;',
						istyle: c.align == 'right' ? 'padding-right:16px' : ''
					});
				}
				rows[i] = ts.header.apply({
					tstyle: 'width:' + tw + ';',
					cells: cells.join('')
				});
			}
		}
		rows[rows.length] = renderHeaders.call(this);
		
		return rows.join('');
	},

	getGroupStyle: function(c) {
		var w = 0, h = true;
		for (var i = c.col; i < c.col + c.colspan; i++) {
			if (!this.cm.isHidden(i)) {
				var cw = this.cm.getColumnWidth(i);
				if(typeof cw == 'number'){
					w += cw;
				}
				h = false;
			}
		}
		return {
			width: (Ext.isBorderBox ? w : Math.max(w - this.borderWidth, 0)) + 'px',
			hidden: h
		}
	},

	updateGroupStyles: function(col) {
		var tables = this.mainHd.query('.x-grid3-header-offset > table'), tw = this.getTotalWidth();
		if(this.cm.rows){
			for (var i = 0; i < tables.length; i++) {
				tables[i].style.width = tw;
				if (i < this.cm.rows.length) {
					var cells = tables[i].firstChild.firstChild.childNodes;
					for (var j = 0; j < cells.length; j++) {
						var c = this.cm.rows[i][j];
						if ((typeof col != 'number') || (col >= c.col && col < c.col + c.colspan)) {
							var gs = this.getGroupStyle(c);
							cells[j].style.width = gs.width;
							cells[j].style.display = gs.hidden ? 'none' : '';
						}
					}
				}
			}
		}
	},

	getHeaderCell : function(index){
		return this.mainHd.query('td.x-grid3-cell')[index];
	},

	updateSortIcon : function(col, dir){
		var sc = this.sortClasses;
		var hds = this.mainHd.select('td.x-grid3-cell').removeClass(sc);
		hds.item(col).addClass(sc[dir == "DESC" ? 1 : 0]);
	}
});

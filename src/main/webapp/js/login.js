Ext.define('czy.view.form.LoginForm', {
    extend: 'Ext.form.Panel',
    xtype: 'loginform',
    width: 300,
    bodyPadding: 10,
    defaults:{
    	labelWidth:60,
    	width:200,
    	labelAlign:'right'
    },
    defaultType: 'textfield',
    items: [{
        allowBlank: false,
        fieldLabel: '用户名',
        name: 'user',
        emptyText: '用户名',
        msgTarget: 'under'
    }, {
        allowBlank: false,
        fieldLabel: '密码',
        name: 'pass',
        emptyText: '密码',
        inputType: 'password'
    }, {
        xtype:'checkbox',
        fieldLabel: '记住我',
        name: 'remember'
    }],
    buttons: [{ 
    	text:'登录',
    	handler:function(){
    		Ext.Msg.alert("提示"," 确定登录？");
    	}
    }]
});
var lwin = Ext.create('Ext.window.Window', {
	title: '登录',
    height: 220,
    width: 300,
    layout: 'fit',
    items: { 
        xtype: 'loginform'
    }
});
Ext.onReady(function(){
	Ext.create('Ext.container.Viewport', {
		layout: 'fit',
	    items: [lwin]
	});
	lwin.show();
});
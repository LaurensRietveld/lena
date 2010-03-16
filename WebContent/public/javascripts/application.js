var sb;
var isSBChanged;
var appPath;
var checked = false;

window.addEvent('domready', function() {

			// $$('.tooltip').each(function(element, index) {
			// var content = element.get('title').split('::');
			// element.store('tip:title', content[0]);
			// element.store('tip:text', content[1]);
			// });

			var tooltip = new Tips($$('.tooltip'), {
						showDelay : 500
					});

			$slide = new Fx.Slide('sidebar', {
						mode : 'horizontal'
					});

			if (sb == false) {
				$slide.hide();
			}

			$slide.addEvent('onStart', function() {
						if (sb == false) {
							$('selection').setStyle('left', '220px');
						}
					});

			$slide.addEvent('onComplete', function() {
						if (sb == true) {
							$('icon').setProperty('src',
									'public/images/control_fastforward.png');
							$('icon').setProperty('title', 'Show sidebar!');
							$('selection').setStyle('left', '10px');
							sb = false;
							isSBChanged = true;
						} else {
							$('icon').setProperty('src',
									'public/images/control_rewind.png');
							$('icon').setProperty('title', 'Hide sidebar!');
							sb = true;
							isSBChanged = true;
						}
					});

		});

function requestAjaxCalls(contextPath, paramURI, paramClass, paramLocation, paramMeta,
		paramBookmark, paramRank, paramStatementLimit, paramDepthLimit,
		paramLinkLimit, paramFactor) {
	var ajaxUrlClasses = '?resource=' + paramURI + '&ajaxClasses=true';
	var ajaxUrlContent = '?resource=' + paramURI + '&class=' + paramClass
			+ '&location=' + paramLocation + '&meta=' + paramMeta
			+ '&bookmark=' + paramBookmark + '&ajaxContent=true';
	var ajaxUrlFacets = contextPath+'/FacetsServlet?resource=' + paramURI 
			+ '&statementLimit=' + paramStatementLimit + '&depthLimit='
			+ paramDepthLimit + '&linkLimit=' + paramLinkLimit + '&factor='
			+ paramFactor + '&ajaxRank=true';
	var ajaxUrlTime = '?ajaxTime=true';
	var ajaxUrlTime2 = '?ajaxTime2=true';
	var ajaxUrlTime3 = '?ajaxTime3=true';
	if (paramRank == 'true') {
		new Request({
					url : ajaxUrlFacets,
					method : 'get',
					update : $('sidebar_right_content'),
					onRequest : function() {
						$('sidebar_right_content').empty()
								.addClass('ajax-loading');
					},
					onComplete : function() {
						$('sidebar_right_content').removeClass('ajax-loading');
						eval($('sidebar_right_content'));
					},
					onSuccess : function(responseText, responseXML) {
						// $exec(responseText);
						$('sidebar_right_content').set('html', responseText);
						var tooltip = new Tips($$('.tooltip'), {
									showDelay : 500
								});
					},
					onCancel : function() {
						$('sidebar_right_content').removeClass('ajax-loading');
					},
					evalScripts : false,
					evalResponse : true
				}).send();
		// new Ajax(ajaxUrlTime, {
		// evalResponse : true,
		// method : 'get',
		// update : $('times'),
		// onRequest : function() {
		// $('times').empty().addClass('ajax-loading-time');
		// },
		// onComplete : function() {
		// $('times').removeClass('ajax-loading-time');
		// },
		// onCancel : function() {
		// $('times').removeClass('ajax-loading-time');
		// }
		// }).request();
		// new Ajax(ajaxUrlTime2, {
		// evalResponse : true,
		// method : 'get',
		// update : $('times2'),
		// onRequest : function() {
		// $('times2').empty().addClass('ajax-loading-time');
		// },
		// onComplete : function() {
		// $('times2').removeClass('ajax-loading-time');
		// },
		// onCancel : function() {
		// $('times2').removeClass('ajax-loading-time');
		// }
		// }).request();
		// new Ajax(ajaxUrlTime3, {
		// evalResponse : true,
		// method : 'get',
		// update : $('times3'),
		// onRequest : function() {
		// $('times3').empty().addClass('ajax-loading-time');
		// },
		// onComplete : function() {
		// $('times3').removeClass('ajax-loading-time');
		// },
		// onCancel : function() {
		// $('times3').removeClass('ajax-loading-time');
		// }
		// }).request();
	}
	new Request({
				url : ajaxUrlClasses,
				method : 'get',
				update : $('menuRemote'),
				onRequest : function() {
					$('menuRemote').empty().addClass('ajax-loading');
					$('selection').empty().addClass('ajax-loading');
				},
				onComplete : function() {
					$('menuRemote').removeClass('ajax-loading');
					new Request({
								url : ajaxUrlContent,
								method : 'get',
								update : $('selection'),
								onComplete : function() {
									$('selection').removeClass('ajax-loading');
								},
								onSuccess : function(responseText, responseXML) {
									// $exec(responseText);
									$('selection').set('html', responseText);
									var tooltip = new Tips($$('.tooltip'), {
												showDelay : 500
											});
								},
								onCancel : function() {
									$('selection').removeClass('ajax-loading');
								},
								evalScripts : false,
								evalResponse : true
							}).send();
				},
				onSuccess : function(responseText, responseXML) {
					// $exec(responseText);
					$('menuRemote').set('html', responseText);
					var tooltip = new Tips($$('.tooltip'), {
								showDelay : 500
							});
				},
				onCancel : function() {
					$('menuRemote').removeClass('ajax-loading');
				},
				evalScripts : false,
				evalResponse : true
			}).send();
}

function toggleSidebar() {
	if (sb == true) {
		$slide.slideOut();
	} else {
		$slide.slideIn();
	}
}

function setSidebar(status) {
	sb = status;
}

function sidebarChanged(value) {
	if (value == true) {
		isSBChanged = true;
	} else {
		isSBChanged = false;
	}
}

function setHrefs() {
	if (isSBChanged == true) {
		$('selection').getElements('a').each(function(item, index) {
			item.setProperty('href', item.getProperty('href') + '&sidebar='
							+ sb);
		});
		$('lenses').getElements('a').each(function(item, index) {
			item.setProperty('href', item.getProperty('href') + '&sidebar='
							+ sb);
		});
		$('rclasses').getElements('a').each(function(item, index) {
			item.setProperty('href', item.getProperty('href') + '&sidebar='
							+ sb);
		});
		$('lclasses').getElements('a').each(function(item, index) {
			item.setProperty('href', item.getProperty('href') + '&sidebar='
							+ sb);
		});
		$('home').setProperty('href',
				$('home').getProperty('href') + '?sidebar=' + sb);
		$('reload').setProperty('href',
				$('reload').getProperty('href') + '&sidebar=' + sb);
	}
}

function reload() {
	var call = new Ajax('../manager/reload?path=/lena-1.1.2', {
				method : 'get'
			});
	call.request();
	call.addEvent('onComplete', function() {
		if (checked == true)
			window.location.href = '?reload=true';
		else
			window.location.href = '?reload=true';
			// '?resource=' + URLencode(resource) + '&lens=' + URLencode(lens) +
			// '&location=' + location + '&reload=true';
		});
}

function URLencode(sStr) {
	return escape(sStr).replace(/\//g, '%2F').replace(/\+/g, '%2B').replace(
			/\"/g, '%22').replace(/\'/g, '%27');
}

function setAppPath(appPath) {
	appPath = appPath;
}

function setChecked(value) {
	checked = value;
}
function checkboxChanged() {
	var ref;
	if (document.getElementById("show_metaknowledge").checked) {
		for (var i = 0; i < document.getElementsByTagName("div").length; i++) {
			if (document.getElementsByTagName("div")[i].getAttribute("id") == "metaknowledge")
				window.document.getElementsByTagName("div")[i].style.display = "";
		}

		if (checked == false) {
			checked = true;
			var pref;

			$('lenses').getElements('a').each(function(item, index) {
						pref = item.getProperty('href');
						pref = pref.substring(0, pref.length - 11)
								+ "&meta=true";
						item.setProperty('href', pref);
					});
			$('rclasses').getElements('a').each(function(item, index) {
						pref = item.getProperty('href');
						pref = pref.substring(0, pref.length - 11)
								+ "&meta=true";
						item.setProperty('href', pref);
					});
			$('lclasses').getElements('a').each(function(item, index) {
						pref = item.getProperty('href');
						pref = pref.substring(0, pref.length - 11)
								+ "&meta=true";
						item.setProperty('href', pref);
					});

		}
		windows.location.href = ref;

	} else if (!document.getElementById("show_metaknowledge").checked) {
		for (var i = 0; i < document.getElementsByTagName("div").length; i++) {
			if (document.getElementsByTagName("div")[i].getAttribute("id") == "metaknowledge")
				window.document.getElementsByTagName("div")[i].style.display = "none";
		}

		if (checked == true) {
			checked = false;
			var pref;

			$('lenses').getElements('a').each(function(item, index) {
						pref = item.getProperty('href');
						pref = pref.substring(0, pref.length - 10)
								+ "&meta=false";
						item.setProperty('href', pref);
					});
			$('rclasses').getElements('a').each(function(item, index) {
						pref = item.getProperty('href');
						pref = pref.substring(0, pref.length - 10)
								+ "&meta=false";
						item.setProperty('href', pref);
					});
			$('lclasses').getElements('a').each(function(item, index) {
						pref = item.getProperty('href');
						pref = pref.substring(0, pref.length - 10)
								+ "&meta=false";
						item.setProperty('href', pref);
					});

		}
	}
}
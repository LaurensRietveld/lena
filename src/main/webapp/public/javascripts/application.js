var sb;
var isSBChanged;
var appPath;
var checked = false;

window.addEvent('domready', function(){
	var tooltip = new Tips($$('.tooltip'), {
		showDelay: 500,
		className: 'custom'
	});

	$slide = new Fx.Slide('sidebar', {mode: 'horizontal'});
	
	if (sb == false){
		$slide.hide();
	}
	
	$slide.addEvent('onStart', function(){
		if (sb == false) {
			$('selection').setStyle('left', '220px');
		}
	});
	
	$slide.addEvent('onComplete', function(){
		if (sb == true){
			$('icon').setProperty('src', 'public/images/control_fastforward.png');
			$('icon').setProperty('title', 'Show sidebar!');
			$('selection').setStyle('left', '10px');
			sb = false;
			isSBChanged = true;
		} else {
			$('icon').setProperty('src', 'public/images/control_rewind.png');
			$('icon').setProperty('title', 'Hide sidebar!');
			sb = true;
			isSBChanged = true;
		}
	});
	
});

function toggleSidebar(){
	if (sb == true){
		$slide.slideOut();
	} else {
		$slide.slideIn();
	}
}
	
function setSidebar(status){
	sb = status;
}

function sidebarChanged(value){
	if (value == true){
		isSBChanged = true;
	} else {
		isSBChanged = false;
	}
}

function setHrefs(){
	if (isSBChanged == true){
		$('selection').getElements('a').each(function(item, index){
			item.setProperty('href', item.getProperty('href') + '&sidebar=' + sb);
		});
		$('lenses').getElements('a').each(function(item, index){
			item.setProperty('href', item.getProperty('href') + '&sidebar=' + sb);
		});
		$('rclasses').getElements('a').each(function(item, index){
			item.setProperty('href', item.getProperty('href') + '&sidebar=' + sb);
		});	
		$('lclasses').getElements('a').each(function(item, index){
			item.setProperty('href', item.getProperty('href') + '&sidebar=' + sb);
		});		
		$('home').setProperty('href', $('home').getProperty('href') + '?sidebar=' + sb);
		$('reload').setProperty('href', $('reload').getProperty('href') + '&sidebar=' + sb);
	}
}

function reload(){
	var call = new Ajax('../manager/reload?path=/lena-1.1.2', {method: 'get'});
	call.request();
	call.addEvent('onComplete', function(){
		if(checked == true)
			window.location.href = '?reload=true&meta=true';
		else
			window.location.href = '?reload=true&meta=false';
		//'?resource=' + URLencode(resource) + '&lens=' + URLencode(lens) + '&location=' + location + '&reload=true';
	});
}

function URLencode(sStr) {
	return escape(sStr)
		.replace(/\//g, '%2F')
			.replace(/\+/g, '%2B')
				.replace(/\"/g,'%22')
					.replace(/\'/g, '%27');
}

function setAppPath(appPath){
	appPath = appPath;
}

function setChecked(value){
	checked = value;
}
function checkboxChanged()
{	
	var ref;
	if(document.getElementById("show_metaknowledge").checked){
		for (var i = 0; i < document.getElementsByTagName("div").length; i++) {
			if(document.getElementsByTagName("div")[i].getAttribute("id") == "metaknowledge")
				window.document.getElementsByTagName("div")[i].style.display = "";
		}
		
		if(checked == false){
			checked = true;
			var pref; 
			$('selection').getElements('a').each(function(item, index){
				pref = item.getProperty('href');
				pref = pref.substring(0,pref.length - 11) + "&meta=true";
				item.setProperty('href', pref);
			});
			$('lenses').getElements('a').each(function(item, index){
				pref = item.getProperty('href');
				pref = pref.substring(0,pref.length - 11) + "&meta=true";
				item.setProperty('href', pref);
			});
			$('rclasses').getElements('a').each(function(item, index){
				pref = item.getProperty('href');
				pref = pref.substring(0,pref.length - 11) + "&meta=true";
				item.setProperty('href', pref);
			});
			$('lclasses').getElements('a').each(function(item, index){
				pref = item.getProperty('href');
				pref = pref.substring(0,pref.length - 11) + "&meta=true";
				item.setProperty('href', pref);
			});
			
		}
		windows.location.href = ref;
		
	}
	else if(!document.getElementById("show_metaknowledge").checked){
		for (var i = 0; i < document.getElementsByTagName("div").length; i++) {
			if(document.getElementsByTagName("div")[i].getAttribute("id") == "metaknowledge")
				window.document.getElementsByTagName("div")[i].style.display = "none";
		}
		
		if(checked==true){
			checked = false;
			var pref; 
			$('selection').getElements('a').each(function(item, index){
				pref = item.getProperty('href');
				pref = pref.substring(0,pref.length - 10) + "&meta=false";
				item.setProperty('href', pref);
			});
			$('lenses').getElements('a').each(function(item, index){
				pref = item.getProperty('href');
				pref = pref.substring(0,pref.length - 10) + "&meta=false";
				item.setProperty('href', pref);
			});
			$('rclasses').getElements('a').each(function(item, index){
				pref = item.getProperty('href');
				pref = pref.substring(0,pref.length - 10) + "&meta=false";
				item.setProperty('href', pref);
			});
			$('lclasses').getElements('a').each(function(item, index){
				pref = item.getProperty('href');
				pref = pref.substring(0,pref.length - 10) + "&meta=false";
				item.setProperty('href', pref);
			});
			
		}		
	}	
}



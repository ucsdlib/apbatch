function loadBillingMenu(){
	var menuDiv = jQuery('<div></div').css("display","inline-block");
	var ul = jQuery('<ul class="sf-menu"></ul>');
	//home link
	//var home = jQuery('<li></li>').html(jQuery('<a  href="index.jsp"></a>').html("Home"));
	var home = jQuery('<li></li>').html(jQuery('<a id = "homeTab" href=""></a>').html("Home"));
	//bursar link
	//var bursar = jQuery('<li></li>').html(jQuery('<a href="bursar.jsp"></a>').html("Bursar"));
	var bursar = jQuery('<li></li>').html(jQuery('<a id = "bursarTab" href=""></a>').html("Bursar"));
	//var bursar = jQuery('<li></li>').html(jQuery('<a></a>').html("Bursar"));
	//3 transaction queues: pending, new, problem
	var queues = jQuery('<li></li>').append(jQuery('<a></a>').html("Operations"));
	var transactionList = jQuery('<ul></ul>');
	var pending = jQuery('<li></li>').html(jQuery('<a id = "pending" href=""></a>').html("File Content"));
	var newPatrons = jQuery('<li></li>').html(jQuery('<a id= "newPatron" href=""></a>').html("New Patrons"));
	var problems = jQuery('<li></li>').html(jQuery('<a id ="problem" href=""></a>').html("Problem"));
	var outputFile = jQuery('<li></li>').html(jQuery('<a id ="GenerateOutputFile" href=""></a>').html("Build txt File"));
	var sendData = jQuery('<li></li>').html(jQuery('<a id ="sendData" href=""></a>').html("Send txt File"));
	//jQuery(transactionList).append(pending).append(newPatrons).append(problems).append(sendData);
	jQuery(transactionList).append(pending).append(outputFile).append(sendData);
	jQuery(queues).append(transactionList);
	//administration
	var admin = jQuery('<li></li>').append(jQuery('<a id ="ViewTab" href=""></a>').html("View"));
	var adminList = jQuery('<ul></ul>');
	var delItems = jQuery('<li></li>').html(jQuery('<a id = "deletedItems" href=""></a>').html("Deleted Items"));
	var probItems = jQuery('<li></li>').html(jQuery('<a id = "problemItems" href=""></a>').html("Problem Items"));
	var problems = jQuery('<li></li>').html(jQuery('<a href="index.jsp"></a>').html("Problem"));
	jQuery(adminList).append(delItems).append(probItems);
	jQuery(admin).append(adminList);
	//index & vendor coding editing
	var editVendorIndex = jQuery('<li></li>').append(jQuery('<a id="editTab" href="#"></a>').html("Edit"));
	var editVectorIndexList = jQuery('<ul></ul>');
	var editVendors = jQuery('<li></li>').html(jQuery('<a id = "editVendors" href="#"></a>').html("Vendors"));
	var editIndexes = jQuery('<li></li>').html(jQuery('<a id = "editIndexes" href="#"></a>').html("Indexes"));
	jQuery(editVectorIndexList).append(editVendors).append(editIndexes);
	jQuery(editVendorIndex).append(editVectorIndexList);
	var logout = jQuery('<li></li>').append(jQuery('<a id ="logoutTab" href=""></a>').html("Logout"));
	//help link
	var help = jQuery('<li></li>').html(jQuery('<a href="help.jsp"></a>').html("Help"));
	//add everything to the top list
	//jQuery(ul).append(home).append(queues).append(admin).append(session).append(bursar);
	jQuery(ul).append(home).append(queues).append(admin).append(editVendorIndex);
	jQuery(menuDiv).append(ul);
	jQuery('.header').append(menuDiv);
	jQuery("ul.sf-menu").superfish({delay:400,animation:{opacity:'show',height:'show'},speed:'slow'});
	jQuery("ul.sf-menu a:first").css("border-left","0"); //remove the first border-left(css workaround)
}
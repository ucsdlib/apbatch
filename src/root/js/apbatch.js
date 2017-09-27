 $(function(){
	 initActions();
	 var sessionData;
	 /** METHODS **/
		function initActions(){
			loadBillingMenu();
			$('#editVendors_container').hide();
			$('#editIndexes_container').hide();
			$('#buttonsPending').hide();
			$('#buttonsProblem').hide();
			$('#buttonsNewPatron').hide();
			$('#filediv').show();
			$('#indexdiv').hide();
			//file submission
			$("#processFile").bind("click",function(){				
			     $('#emptyTable').remove();
			     $('#editVendors_grid').empty();
			     $('#editVendors_container').hide();
			     $('#editIndexes_grid').empty();
			     $('#editIndexes_container').hide();
				$.blockUI({ message: '<img src="images/busy.gif" /> Processing AP Batch out File. Please wait..' });
				$('.container').hide(); //hide everything before new search
				//$('.data_container').hide(); 
				$.ajaxFileUpload({
					url:"/apbatch/servlets/UploadOutFile", 
					secureuri:true,
					fileElementId:'fileToUpload',
					fileArk: '',
					dataType: 'json',
					success: function(data){
					sessionData = data;
					$.unblockUI();
					$('#filediv').hide();
					$('#actions').hide();
					$('#indexdiv').show();
					$('#buttonsPending').show();
					init(data);
						},
					error: function (data, status, e){
						var errorMessage = data.responseText;
						if(errorMessage.indexOf("message") < 0) {
						   errorMessage = 'There was an error or timeout processing the Billing File.  ' + e;
						} else {
						   errorMessage = errorMessage.substring(errorMessage.indexOf("message")+7,errorMessage.indexOf("description"));
						}
						//add error message
						$.blockUI({ message: errorMessage});
						 setTimeout($.unblockUI, 2000);
					}
				});
				
				return false;
			});
			
			
			$("#pending").bind("click",function(){
				$.blockUI({ message: '<img src="images/busy.gif" /> Getting data..Please wait..' });
				 $('#emptyTable').remove();
				 $("table.dataGrid tbody").empty();
				 $("table.dataGrid thead").show();
			     $('#editVendors_grid').empty();
			     $('#editVendors_container').hide();
			     $('#editIndexes_grid').empty();
			     $('#editIndexes_container').hide();
				$('#filediv').hide();
				$('#actions').hide();
				$('#indexdiv').show();	
				$('#buttonsPending').show();
				$('#buttonsProblem').hide();
				//alert(sessionData.total);
				init(sessionData);
				return false;
			});
			

			$("#homeTab").bind("click",function(){
				//$("#emptyTable").remove();
			    $('#editVendors_grid').empty();
			    $('#editVendors_container').hide();
			    $('#editIndexes_grid').empty();
			    $('#editIndexes_container').hide();
				$('#indexdiv').hide();				
				$('#filediv').show();
				$('#actions').show();
				
				return false;
			});
			
			
			$("#logout").bind("click",function(){
				window.location = "/apbatch/logout.jsp";
				//return false;
			});
			
			$("#GenerateOutputFile").bind("click",function(){
				processOutputFile();
				return false;
			});
			$("#sendData").bind("click",function(){
				sendDataToServer();
				
				//confirmSendData();
				return false;
			});		
		
function sendDataToServer()
			 {
			 //var agree=confirm("Are you sure you want to send data to server?");
			 var agree=confirm("Have you built the txt file yet?");
			 var temp;
			 if (agree)
			 {
				$("#results").empty();
						
					 $.blockUI({ message: '<img src="images/busy.gif" /> Sending Data...' });
					 $.ajax({
							url: "/apbatch/servlets/SendOutputFiles", 
							dataType: 'json',
							//data:{username:username,password:password},
							success: displaySendFileLog,
							error:function (data, status, e){
								$.unblockUI();
								//add error message
								$.blockUI({ message: 'There was an error or timeout when calling SendOutputFiles servlet : ' + data.errorMsg});
								 setTimeout($.unblockUI, 5000);
							}
						});					
				
			 }
			 else
			 {
				// displayPendingQueueConfirmNo(jsonData); 
				 return false ;
			 }
			
			 }			
	 /*function confirmSendData()
			 {
			 var agree=confirm("Have you built the txt file yet?");
			 var temp;
			 if (agree)
			 	{
				// var username = prompt("Please enter your username for FTP server","");
				// var temp = prompt("Please enter your password for FTP server","");
					
				 //
				 var w = 480, h = 340;
				 if (document.all) {
					  
					   w = document.body.clientWidth;
					   h = document.body.clientHeight;
					}
					else if (document.layers) {
					   w = window.innerWidth;
					   h = window.innerHeight;
					}
				 var popW = 300, popH = 200;
				 var leftPos = (w-popW)/2, topPos = (h-popH)/2;
	
				 wleft = (screen.width - w) / 2;
				  wtop = (screen.height - h) / 2;
				 var Text ='';
					Text+='<html> <head>';
					Text += '<script type="text/javascript" src="js/apbatch.js"></script>';	
					Text += '<script language="Javascript" type="text/javascript">';
				   	Text+= 'function CallParentWindowFunction(){ ';
				   	Text += ' var name=document.getElementById("txtUsername").value; ';
				   	Text += 'var pp=document.getElementById("txtpassword").value; ';
				 	Text += ' window.opener.getPassword(name,pp);window.close(); return true;';
				   	Text+= '}</script>';
					Text+= '</head> <body>';
					Text += ' <table><tr>Please enter FTP username and password</tr><tr><td>Username:</td> ';
					Text += '<td><input type="text" id="txtUsername"></td></tr>';
					Text += ' <tr><td>Password:</td> ';
					Text += ' <td><input type="password" id="txtpassword"></td></tr>';
					Text += '<tr><td><input type="submit" value= "Ok"  onClick="CallParentWindowFunction()"></td>';
					Text += '<td><input type="submit" value= "Cancel"  onClick="window.close()"></td></tr>';
					Text+='</table></body></html>';
					
						win3 = window.open("","newwin",'width=' + popW + ',height='+popH+',top='+wtop+',left='+wleft);
					//  win3 = window.open("","newwin","width=350,height=150"); 
					    var tmp = win3.document;
						tmp.write(Text);					
						tmp.close();
						//win3.moveTo(wleft, wtop);
						win3.focus();
						document.body.style.cursor = 'default'; 
						
						
						
				
			 	}
			 else
			 {
				// displayPendingQueueConfirmNo(jsonData); 
				 return false ;
			 }
			
			 }		*/	
			
			//select All button action for pending queue
			$("#selectAllBtn").bind("click",function(){		
				
				//var checked_status = this.checked;
				$("input[@id=chkDisplay]").each(function()
				{
					this.checked = true;
				});
				return false;
			});	
			//deselect All button action for pending queue
			$("#deselectAllBtnPending").bind("click",function(){		
				
				//var checked_status = this.checked;
				$("input[@id=chkDisplay]").each(function()
				{
					this.checked = false;
				});
				return false;
			});	
			//select All button action for Problem queue
			$("#selectAllProbBtn").bind("click",function(){		
				
				//var checked_status = this.checked;
				$("input[@id=chkDisplay]").each(function()
				{
					this.checked = true;
				});
				return false;
			});	
			
			//deselect All button action for Problem queue
			$("#deselectAllBtnProblem").bind("click",function(){		
				
				//var checked_status = this.checked;
				$("input[@id=chkDisplay]").each(function()
				{
					this.checked = false;
				});
				return false;
			});	
			  //Action event for delete button in the Pending Queue
			 $("#delBtnPen").bind("click",function(){
				 //var selector_checked = $("input[@id=chkDisplay]:checked").length;
				 var selector_checked = $("input[@id=chkDisplayVoucher]:checked").length;  
				 if(selector_checked == 0)
				 {
					 alert("Please select at least one record!");
				 }
				 else if(selector_checked > 0 )
				 {
					 //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
					 //var valArray = $("input[id='chk']").fieldArray();
					 //var myString = $("input[id='chkDisplay']").getValue();
					 var myString = $("input[id='chkDisplayVoucher']").getValue();
					 var valArray = myString.tokenize(",", " ", true);
					 tokensPending = valArray;
					 var whichQueue = 'P';
					 confirmSubmitPending(myString);
							
				 } 
				
				});	 
				$("#deletedItems").bind("click",function(){	
				    $('#emptyTable').remove();
					$("table.dataGrid tbody").empty();
					$("table.dataGrid thead").show();
					//$("table.dataGrid.thead").show();
					//$('.data_container').hide(); 
					$('#filediv').hide();
					$('#actions').hide();
					$('#indexdiv').show();
					$('#buttonsPending').hide();
					$('#buttonsProblem').hide();
				    $('#editVendors_grid').empty();
				    $('#editVendors_container').hide();
				    $('#editIndexes_grid').empty();
				    $('#editIndexes_container').hide();
					//var p = $("<p></p>").html("Deleted Items");
					
					initDel(sessionData);
				return false;
				});	
				
                    $("#problemItems").bind("click",function(){	
					//alert("hi");
                	 $('#emptyTable').remove();
						$("table.dataGrid tbody").empty();
					    $('#editVendors_grid').empty();
					    $('#editVendors_container').hide();
					    $('#editIndexes_grid').empty();
					    $('#editIndexes_container').hide();
						$("table.dataGrid thead").show();
						$('#filediv').hide();
						$('#actions').hide();
						$('#buttonsPending').hide();
						$('#indexdiv').show();
						$('#buttonsProblem').show();
					$.ajax({
						url: "/apbatch/servlets/GetSessionData", 
						dataType: 'json',
						success:displayProbQueue,						
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling GetSessionData servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});	
				
				
					
					return false;
				});
				$("#editVendors").bind("click",function(){
					$('#indexdiv').hide();
					$('#actions').hide();
					$('#editVendors_container').show();
					$('#editIndexes_container').hide();
					$.ajax({
						url: "/apbatch/servlets/EditVendors",
						dataType: 'json',
						data: {EDIT_VENDORS_REQUEST:"DISPLAY"},
						success:editVendors_setUpDisplay,
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling EditVendors servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});
				});
				$.fn.slideFadeToggle = function(easing, callback) {
				    return this.animate({ opacity: 'toggle', height: 'toggle' }, "fast", easing, callback);
				};
				$('#editVendors_add').bind('click', function() {
					if($('.messagepop').length)
						return false;
					$('#editVendors_dialogModal').append('<div class="messagepop pop">' +
						'<table border="0">' +
							'<tr>' +
								'<td class="eva_align_labels"><label for="eva_vendor_name">Vendor Name</label></td>' +
								'<td class="eva_popup_spacing"><input type="text" id="eva_vendor_name" /></td>' +
							'</tr><tr>' +
							'<td class="eva_align_labels"><label for="eva_vendor_code">Vendor Code</label></td>' +
                    		'<td class="eva_popup_spacing"><input type="text" id="eva_vendor_code" size="5"/></td>' +
							'</tr><tr>' +
							'<td class="eva_align_labels"><label for="eva_payeeid">FinancialLink Payee ID</label></td>' +
							'<td class="eva_popup_spacing"><input type="text" id="eva_payeeid" size="9"/></td>' +
							'</tr><tr>' +
							'<td class="eva_align_labels"><label for="eva_addrtype">Address Type</label></td>' +
							'<td class="eva_popup_spacing"><input type="text" id="eva_addrtype" size="2"/></td>' +
							'</tr><tr>' +
							'<td class="eva_align_labels"><label for="eva_taxcode">Tax Code</label></td>' +
							'<td class="eva_popup_spacing"><select id="eva_taxcode"><option id="s_000">000</option><option id="s_008">008</option></select></td>' +
							'</tr><tr>' +
							'<td class="eva_align_labels"><label for="eva_notes">Notes</label></td>' +
							'<td class="eva_popup_spacing"><textarea id="eva_notes" cols="30"></textarea></td>' +
							'</tr><tr>' +
							'<td><input type="button" value="Save" id="eva_popup_send" class="eva_buttons_alignment_left "/></td><td><input type="button" value="Cancel" id="eva_popup_close" class="eva_buttons_alignment_right "/></td>' +
							'</tr></table></div><div class="editVendors_overlay"></div>');
			        $(".pop").slideFadeToggle();
			        $('#eva_vendor_name').focus();
			        $('#eva_popup_send').click(function() {
						if($.trim($('#eva_vendor_code').val()).length > 5) {
							$.unblockUI();
							$.blockUI({ message: 'Vendor Code must be no more than 5 characters'});
							setTimeout($.unblockUI, 500);
						} else if($.trim($('#eva_payeeid').val()).length != 9) {
							$.unblockUI();
							$.blockUI({ message: 'FinancialLink Payee ID must be exactly 9 characters'});
							setTimeout($.unblockUI, 500);
						} else if($.trim($('#eva_addrtype').val()).length != 2) {
							$.unblockUI();
							$.blockUI({ message: 'Address Type must be exactly 2 characters'});
							setTimeout($.unblockUI, 500);
						} else {
							$.ajax({
								url: "/apbatch/servlets/EditVendors",
								dataType: 'json',
								data: {EDIT_VENDORS_REQUEST:"ADD", vendor_name: $('#eva_vendor_name').val(),
										vendor_code: $('#eva_vendor_code').val(),
										payee_id: $('#eva_payeeid').val(),
										address_type: $('#eva_addrtype').val(),
										tax_code: $('#eva_taxcode').val(),
										notes: $('#eva_notes').val()
								},
								success:editVendors_insert,
								error:function (obj,text,error) {
									$.unblockUI();
									$.blockUI({ message: obj.responseText });
									 setTimeout($.unblockUI, 2000);
								}
							});
						}
			        });
					$("#eva_popup_close").click(function() {
					    $(".pop").slideFadeToggle();
					    $("#editVendors_dialogModal").empty();
					});
					return false;
				});
				$('#editVendors_edit').bind('click', function() {
					if($('.messagepop').length)
						return false;
					var tmp = $('input[name="editVendors_checkboxes"]:checked');
					if(tmp.length === 1) {
						$.ajax({
							url: "/apbatch/servlets/EditVendors",
							dataType: 'json',
							data: {EDIT_VENDORS_REQUEST:"FETCH", vendor_id: tmp.val()},
							success: editVendors_editPopUp,
							error:function (obj,text,error) {
								$.unblockUI();
								$.blockUI({ message: obj.responseText });
								 setTimeout($.unblockUI, 2000);
							}
						});
					} else {
						$.unblockUI();
						$.blockUI({ message: 'Select 1 entry to edit'});
						setTimeout($.unblockUI, 500);
					}
					return false;
				});
				$("#editVendors_delete").bind("click",function(){
					var tmp = $('input[name="editVendors_checkboxes"]:checked');
					if(tmp.length > 0) {
						if($('.messagepop').length)
							return false;

						$('#editVendors_dialogModal').append('<div class="messagepop pop">' +
							'<table border="0">' +
								'<tr>' +
								'<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>' +
								'<td><input type="button" value="Delete items" id="eva_popup_send" class="eva_buttons_alignment_left"/></td><td><input type="button" value="Cancel delete" id="eva_popup_close" class="eva_buttons_alignment_right"/></td>' +
								'</tr></table></div><div class="editVendors_overlay"></div>');
			        	$(".pop").slideFadeToggle();
			        	$('#eva_popup_send').click(function() {
						    $(".pop").slideFadeToggle();
						    $("#editVendors_dialogModal").empty();
			        		$.ajax({
								url: "/apbatch/servlets/EditVendors",
								dataType: 'json',
								data: {EDIT_VENDORS_REQUEST:"DELETE", vendor_id: tmp.getValue()},
								success: editVendors_deleteEntries,
								error:function (data, status, e){
									$.unblockUI();
									//add error message
									$.blockUI({ message: 'There was an error or timeout when caling EditVendors servlet' });
									setTimeout($.unblockUI, 2000);
								}
							});
			        	});
						$("#eva_popup_close").click(function() {
						    $(".pop").slideFadeToggle();
						    $("#editVendors_dialogModal").empty();
						    $('input[name="editVendors_checkboxes"]:checked').each(function() {
						    	$(this).attr('checked', false);
						    });
						});
					} else {
						$.unblockUI();
						$.blockUI({ message: 'Select at least 1 entry to delete'});
						setTimeout($.unblockUI, 500);
					}
				});



				$("#editIndexes").bind("click",function(){
					$('#indexdiv').hide();
					$('#actions').hide();
					$('#editVendors_container').hide();
					$('#editIndexes_container').show();
					$.ajax({
						url: "/apbatch/servlets/EditIndexes",
						dataType: 'json',
						data: {EDIT_INDEXES_REQUEST:"DISPLAY"},
						success:editIndexes_setUpDisplay,
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling EditIndexes servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});
				});
				$.fn.slideFadeToggle = function(easing, callback) {
				    return this.animate({ opacity: 'toggle', height: 'toggle' }, "fast", easing, callback);
				};
				$('#editIndexes_add').bind('click', function() {
					if($('.messagepop').length)
						return false;
					$('#editIndexes_dialogModal').append('<div class="messagepop pop">' +
						'<table border="0">' +
							'<tr>' +
							'<td class="eia_align_labels"><label for="eia_index">Index</label></td>' +
                    		'<td class="eia_popup_spacing"><input type="text" id="eia_index" size="7"/></td>' +
							'</tr><tr>' +
							'<td class="eia_align_labels"><label for="eia_fund">Fund</label></td>' +
							'<td class="eia_popup_spacing"><input type="text" id="eia_fund" size="6"/></td>' +
							'</tr><tr>' +
							'<td class="eia_align_labels"><label for="eia_org">Organization</label></td>' +
							'<td class="eia_popup_spacing"><input type="text" id="eia_org" size="6"/></td>' +
							'</tr><tr>' +
							'<td class="eia_align_labels"><label for="eia_prog">Program</label></td>' +
							'<td class="eia_popup_spacing"><input type="text" id="eia_prog" size="6"></td>' +
							'</tr><tr>' +
							'<td class="eia_align_labels"><label for="eia_title">Description</label></td>' +
							'<td class="eia_popup_spacing"><textarea id="eia_title" cols="30"></textarea></td>' +
							'</tr><tr>' +
							'<td><input type="button" value="Save" id="eia_popup_send" class="eia_buttons_alignment_left "/></td><td><input type="button" value="Cancel" id="eia_popup_close" class="eia_buttons_alignment_right "/></td>' +
							'</tr></table></div><div class="editIndexes_overlay"></div>');
			        $(".pop").slideFadeToggle();
			        $('#eia_index').focus();
			        $('#eia_popup_send').click(function() {
						if($.trim($('#eia_index').val()).length != 7 ||
								$.trim($('#eia_index').val()).substring(0,3) != 'LIB') {
							$.unblockUI();
							$.blockUI({ message: 'Index must be exactly 7 characters and start with "LIB"'});
							setTimeout($.unblockUI, 500);
						} else if($.trim($('#eia_fund').val()).length != 6 ||
								/*!editIndexes_checkIsNumeric('#eia_fund', 0, 5) ||*/
								!editIndexes_checkIsAlpha('#eia_fund', 5, 1)) {
							$.unblockUI();
							/*$.blockUI({ message: 'Fund must be exactly 6 characters, where the first 5 are digits and the last is a letter'});*/
							$.blockUI({ message: 'Fund must be exactly 6 characters, where the last is a letter'});
							setTimeout($.unblockUI, 500);
						} else if($.trim($('#eia_org').val()).length != 6 ||
								!editIndexes_checkIsNumeric('#eia_org', 0, -1)) {
							$.unblockUI();
							$.blockUI({ message: 'Organization must be exactly 6 digits'});
							setTimeout($.unblockUI, 500);
						} else if($.trim($('#eia_prog').val()).length != 6 ||
								!editIndexes_checkIsNumeric('#eia_prog', 0, -1)) {
							$.unblockUI();
							$.blockUI({ message: 'Program must be exactly 6 digits'});
							setTimeout($.unblockUI, 500);
						} else {
							$.ajax({
								url: "/apbatch/servlets/EditIndexes",
								dataType: 'json',
								data: {EDIT_INDEXES_REQUEST:"ADD",
										index: $('#eia_index').val(),
										fund: $('#eia_fund').val(),
										org: $('#eia_org').val(),
										prog: $('#eia_prog').val(),
										title: $('#eia_title').val()
								},
								success:editIndexes_insert,
								error:function (obj,text,error) {
									$.unblockUI();
									$.blockUI({ message: obj.responseText });
									 setTimeout($.unblockUI, 2000);
								}
							});
						}
			        });
					$("#eia_popup_close").click(function() {
					    $(".pop").slideFadeToggle();
					    $("#editIndexes_dialogModal").empty();
					});
					return false;
				});
				$('#editIndexes_edit').bind('click', function() {
					if($('.messagepop').length)
						return false;
					var tmp = $('input[name="editIndexes_checkboxes"]:checked');
					if(tmp.length === 1) {
						$.ajax({
							url: "/apbatch/servlets/EditIndexes",
							dataType: 'json',
							data: {EDIT_INDEXES_REQUEST:"FETCH", index: tmp.val()},
							success: editIndexes_editPopUp,
							error:function (obj,text,error) {
								$.unblockUI();
								$.blockUI({ message: obj.responseText });
								 setTimeout($.unblockUI, 2000);
							}
						});
					} else {
						$.unblockUI();
						$.blockUI({ message: 'Select 1 entry to edit'});
						setTimeout($.unblockUI, 500);
					}
					return false;
				});
				$("#editIndexes_delete").bind("click",function(){
					var tmp = $('input[name="editIndexes_checkboxes"]:checked');
					if(tmp.length > 0) {
						if($('.messagepop').length)
							return false;

						$('#editIndexes_dialogModal').append('<div class="messagepop pop">' +
							'<table border="0">' +
								'<tr>' +
								'<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>' +
								'<td><input type="button" value="Delete items" id="eva_popup_send" class="eva_buttons_alignment_left"/></td><td><input type="button" value="Cancel delete" id="eva_popup_close" class="eva_buttons_alignment_right"/></td>' +
								'</tr></table></div><div class="editVendors_overlay"></div>');
			        	$(".pop").slideFadeToggle();
			        	$('#eva_popup_send').click(function() {
						    $(".pop").slideFadeToggle();
						    $("#editIndexes_dialogModal").empty();
							$.ajax({
								url: "/apbatch/servlets/EditIndexes",
								dataType: 'json',
								data: {EDIT_INDEXES_REQUEST:"DELETE", index: tmp.getValue()},
								success: editIndexes_deleteEntries,
								error:function (data, status, e){
									$.unblockUI();
									//add error message
									$.blockUI({ message: 'There was an error or timeout when caling EditIndexes servlet' });
									setTimeout($.unblockUI, 2000);
								}
							});
			        	});
						$("#eva_popup_close").click(function() {
						    $(".pop").slideFadeToggle();
						    $("#editIndexes_dialogModal").empty();
						    $('input[name="editIndexes_checkboxes"]:checked').each(function() {
						    	$(this).attr('checked', false);
						    });
						});
					} else {
						$.unblockUI();
						$.blockUI({ message: 'Select at least 1 entry to delete'});
						setTimeout($.unblockUI, 500);
					}
				});




            		//edit button for Problem queue
           		 $("#editBtnProb").bind("click",function(){
           				
           			 var selector_checked = $("input[@id=chkDisplay]:checked").length; 
           			 if(selector_checked == 0)
           			 {
           				 alert("Please select at least one record!");
           			 }
           			 else if(selector_checked == 1 )
           			 {
           				 var myString = $("input[id='chkDisplay']").getValue();			
           							
           				//===ajax call to get data related to that record==============
           				 $.ajax({
           						url: "/apbatch/servlets/GetProblemQueueData", 
           						dataType: 'json',
           						data: {invoiceArr:myString},
           						success: displayEditWindowForProblemQueue,						
           						error:function (data, status, e){
           							$.unblockUI();
           							//add error message
           							$.blockUI({ message: 'There was an error or timeout when caling GetProblemQueueData servlet' });
           							 setTimeout($.unblockUI, 2000);
           						}
           					});		
           			
           			 }
           			 else if(selector_checked > 1 )
           			 {
           				 alert("Please select one record at a time to edit!"); 
           			 }
           			});
           		 
         		//Action event for "move to Pending Queue" button in the Problem Queue
        		 $("#moveToPendQBtn").bind("click",function(){
			     $('#editVendors_grid').empty();
			     $('#editVendors_container').hide();
			     $('#editIndexes_grid').empty();
			     $('#editIndexes_container').hide();
        			 var selector_checked = $("input[@id=chkDisplay]:checked").length; 
        			 if(selector_checked == 0)
        			 {
        				 alert("Please select at least one record!");
        			 }
        			 else if(selector_checked > 0 )
        			 {
        				 //var valArray = $("input[id='chk']").fieldArray();
        				 var myString = $("input[id='chkDisplay']").getValue();
        				 var valArray = myString.tokenize(",", " ", true);
        				 tokensPending = valArray;
        				 
        				 //==========================
        				 $('#emptyTable').remove();
							$("table.dataGrid tbody").empty();
							$("table.dataGrid thead").show();
							$('#filediv').hide();
							$('#actions').hide();
							$('#buttonsPending').hide();
							$('#indexdiv').show();
							$('#buttonsProblem').show();
        				 $.ajax({
        						url: "/apbatch/servlets/MoveToOtherQueues", 
        						dataType: 'json',
        						data: {invoiceArr:myString,whichQueue:'Q'},
        						success: displayProbQueue,
        						error:function (data, status, e){
        							$.unblockUI();
        							//add error message
        							$.blockUI({ message: 'There was an error or timeout when caling MODIFYQUEUES servlet' });
        							 setTimeout($.unblockUI, 2000);
        						}
        					});		 
        				
        			
        			 } 
        			});
        		 
        		 $("#getTotal").bind("click",function(){
        			 $.ajax({
 						url: "/apbatch/servlets/GetTotal", 
 						dataType: 'json',
 						success: function(data){
        				 alert(data.SUM);
        			       },
 						error:function (data, status, e){
 							$.unblockUI();
 							//add error message
 							$.blockUI({ message: 'There was an error or timeout when caling GetTotal servlet' });
 							 setTimeout($.unblockUI, 2000);
 						}
 					});		  
      			});
        		 
        		 //Action event for "move to Problem Queue" button in the Pending Queue
        		 $("#moveToProbQBtn").bind("click",function(){
        			 var selector_checked = $("input[@id=chkDisplay]:checked").length; 
        			 if(selector_checked == 0)
        			 {
        				 alert("Please select at least one record!");
        			 }
        			 else if(selector_checked > 0 )
        			 {
        				 
        				 var myString = $("input[id='chkDisplay']").getValue();
        				 var valArray = myString.tokenize(",", " ", true);
        				 tokensPending = valArray;
        				 
        				 $('#emptyTable').remove();
							$("table.dataGrid tbody").empty();
							$("table.dataGrid thead").show();
							$('#filediv').hide();
							$('#actions').hide();
							$('#buttonsPending').show();
							$('#indexdiv').show();
							$('#buttonsProblem').hide();
        				 //var valArray = $("input[id='chk']").fieldArray();
        				
        				
        				 //==========================
        				 $.blockUI({ message: '<img src="images/busy.gif" /> Moving Items..Please wait..' });
        				 $.ajax({
        						url: "/apbatch/servlets/MoveToOtherQueues", 
        						dataType: 'json',
        						data: {invoiceArr:myString,whichQueue:'P'},
        						success: function(data){
        							sessionData = data;
        							$.unblockUI();
        							init(data);
        								},
        						error:function (data, status, e){
        							$.unblockUI();
        							//add error message
        							$.blockUI({ message: 'There was an error or timeout when caling MoveToOtherQueues servlet' });
        							 setTimeout($.unblockUI, 2000);
        						}
        					});		 
        				
        			
        			 } 
        			});
		}//end if initActions()
		//==================================================================
		function processOutputFile(){
			//window.open("/apbatch/servlets/ProcessOutputData");		
			$.blockUI({ message: '<img src="images/busy.gif" /> Building Txt File...' });
			$.ajax({
				url: "/apbatch/servlets/ProcessOutputData",
				dataType: 'text',				
				success: displayBuildFile,
				error:function (data, status, e){
					$.unblockUI();
					//add error message
					$.blockUI({ message: 'There was an error or timeout when building Txt File:'+e});
					setTimeout($.unblockUI, 5000);
				}				
			});
		}
		function editVendors_setUpDisplay(data, status) {
			$.unblockUI();
			$('#editVendors_grid').html(data.htmlStr);
			$('#editVendors_table_body').children().each(function() {
				$(this).qtip({
					content: $(this).attr('tooltip'),
					position: {
						corner: {
							tooltip: 'leftMiddle',
							target: 'rightMiddle'
						}
					},
					style: 'dark'
				});
			});
		}
		function editVendors_insert(data, status) {
			$.unblockUI();
			editVendors_appendInOrder(data);
			var tmp_notes = $('#editVendors_checkbox_' + data.vendor_id).attr('tooltip');
			$('#editVendors_checkbox_' + data.vendor_id).qtip({
				content: tmp_notes,
				position: {
					corner: {
						tooltip: 'leftMiddle',
						target: 'rightMiddle'
					}
				},
				style: 'dark'
			});
			$(".pop").slideFadeToggle();
			$("#editVendors_dialogModal").empty();
		}
		function editVendors_editPopUp(data, status) {
			$('#editVendors_dialogModal').append('<div class="messagepop pop">' +
				'<table border="0">' +
					'<tr>' +
						'<td class="eva_align_labels"><label for="eva_vendor_name">Vendor Name</label></td>' +
						'<td class="eva_popup_spacing"><input type="text" id="eva_vendor_name" /></td>' +
					'</tr><tr>' +
					'<td class="eva_align_labels"><label for="eva_vendor_code">Vendor Code</label></td>' +
            		'<td class="eva_popup_spacing"><input type="text" id="eva_vendor_code" size="5"/></td>' +
					'</tr><tr>' +
					'<td class="eva_align_labels"><label for="eva_payeeid">FinancialLink Payee ID</label></td>' +
					'<td class="eva_popup_spacing"><input type="text" id="eva_payeeid" size="9"/></td>' +
					'</tr><tr>' +
					'<td class="eva_align_labels"><label for="eva_addrtype">Address Type</label></td>' +
					'<td class="eva_popup_spacing"><input type="text" id="eva_addrtype" size="2"/></td>' +
					'</tr><tr>' +
					'<td class="eva_align_labels"><label for="eva_taxcode">Tax Code</label></td>' +
					'<td class="eva_popup_spacing"><select id="eva_taxcode"><option id="s_000">000</option><option id="s_008">008</option></select></td>' +
					'</tr><tr>' +
					'<td class="eva_align_labels"><label for="eva_notes">Notes</label></td>' +
					'<td class="eva_popup_spacing"><textarea id="eva_notes" cols="30"></textarea></td>' +
					'</tr><tr>' +
					'<td><input type="button" value="Save" id="eva_popup_send" class="eva_buttons_alignment_left "/></td><td><input type="button" value="Cancel" id="eva_popup_close" class="eva_buttons_alignment_right "/></td>' +
					'</tr></table></div><div class="editVendors_overlay"></div>');
			$('#eva_vendor_name').val(data.vendor_name);
			$('#eva_vendor_code').val(data.vendor_code);
			$('#eva_payeeid').val(data.payee_id);
			$('#eva_addrtype').val(data.address_type);
			$('#eva_taxcode').val(data.tax_code);
			$('#eva_notes').val(data.notes);

	        $(".pop").slideFadeToggle();
	        $('#eva_vendor_name').focus();
	        $('#eva_popup_send').click(function() {
				if($.trim($('#eva_vendor_code').val()).length > 5) {
					$.unblockUI();
					$.blockUI({ message: 'Vendor Code must be no more than 5 characters'});
					setTimeout($.unblockUI, 500);
				} else if($.trim($('#eva_payeeid').val()).length != 9) {
					$.unblockUI();
					$.blockUI({ message: 'FinancialLink Payee ID must be exactly 9 characters'});
					setTimeout($.unblockUI, 500);
				} else if($.trim($('#eva_addrtype').val()).length != 2) {
					$.unblockUI();
					$.blockUI({ message: 'Address Type must be exactly 2 characters'});
					setTimeout($.unblockUI, 500);
				} else {
					$.ajax({
						url: "/apbatch/servlets/EditVendors",
						dataType: 'json',
						data: {EDIT_VENDORS_REQUEST:"EDIT", vendor_id: data.vendor_id,
								vendor_name: $('#eva_vendor_name').val(),
								vendor_code: $('#eva_vendor_code').val(),
								payee_id: $('#eva_payeeid').val(),
								address_type: $('#eva_addrtype').val(),
								tax_code: $('#eva_taxcode').val(),
								notes: $('#eva_notes').val()
						},
						success:editVendors_updatePopUp,
						error:function (obj,text,error) {
							$.unblockUI();
							$.blockUI({ message: obj.responseText });
							 setTimeout($.unblockUI, 2000);
						}
					});
				}
	        });
			$("#eva_popup_close").click(function() {
			    $(".pop").slideFadeToggle();
			    $("#editVendors_dialogModal").empty();
			});
		}
		function editVendors_updatePopUp(data, status) {
			$.unblockUI();
			$('#editVendors_checkbox_' + data.vendor_id).remove();
			editVendors_appendInOrder(data);
			$(".pop").slideFadeToggle();
			$("#editVendors_dialogModal").empty();
			var tmp_notes = $('#editVendors_checkbox_' + data.vendor_id).attr('tooltip');
			$('#editVendors_checkbox_' + data.vendor_id).qtip({
				content: tmp_notes,
				position: {
					corner: {
						tooltip: 'leftMiddle',
						target: 'rightMiddle'
					}
				},
				style: 'dark'
			});
		}
		function editVendors_appendInOrder(data) {
			var length = $('#editVendors_table_body').children().length;
			if(length != 0) {
				var tr_children = $('#editVendors_table_body > tr');
				var inserted_value = 0;
				$.each(tr_children, function() {
					/* only insert if it hasn't been done */
					if(inserted_value === 0) {
						var cmp_result = data.vendor_name.localeCompare($(this).children('td:nth-child(3)').text());
						if(cmp_result < 0) {
							/* found first position that was less than the name, so insert here */
							$(data.htmlStr).insertBefore($(this));
							inserted_value = 1;
						} else if(cmp_result === 0) {
							/* Equal names, so decide based on their code */
							cmp_result = data.vendor_code.localeCompare($(this).children('td:nth-child(2)').text());
							if(cmp_result < 0) {
								/* found first position that was less than code, so insert here */
								$(data.htmlStr).insertBefore($(this));
								inserted_value = 1;
							}
						}
					}
				});
				if(inserted_value === 0) {
					$('#editVendors_table_body').append(data.htmlStr);
				}
			} else {
				$('#editVendors_table_body').append(data.htmlStr);
			}
		}
		function editVendors_deleteEntries(data, status) {
			var tmp = $('input[name="editVendors_checkboxes"]:checked');
			var pDeletedItems = tmp.getValue().split(',');
			var length = tmp.length;  
			for(var x = 0; x < length; ++x) {
				$('#editVendors_checkbox_' + pDeletedItems[x]).remove();
			}
		}


		function editIndexes_checkIsNumeric(label, start_index, num_chars_to_check) {
			var data = $.trim($(label).val());

			if(num_chars_to_check === -1)
				num_chars_to_check = data.length;

			for(i = 0; i < num_chars_to_check; ++i, ++start_index) {
				if(!(data.charAt(start_index) >= '0' && data.charAt(start_index) <= '9'))
					return false;
			}
			return true;
		}
	
		function editIndexes_checkIsAlpha(label, start_index, num_chars_to_check) {
			var data = $.trim($(label).val());

			if(num_chars_to_check === -1)
				num_chars_to_check = data.length;

			for(i = 0; i < num_chars_to_check; ++i, ++start_index) {
				if(!((data.charAt(start_index) >= 'A' && data.charAt(start_index) <= 'Z') ||
						(data.charAt(start_index) >= 'a' && data.charAt(start_index) <= 'z')))
					return false;
			}
			return true;
		}

		function editIndexes_setUpDisplay(data, status) {
			$.unblockUI();
			$('#editIndexes_grid').html(data.htmlStr);
		}
		function editIndexes_insert(data, status) {
			$.unblockUI();
			editIndexes_appendInOrder(data);
			$(".pop").slideFadeToggle();
			$("#editIndexes_dialogModal").empty();
		}
		function editIndexes_editPopUp(data, status) {
			$('#editIndexes_dialogModal').append('<div class="messagepop pop">' +
				'<table border="0">' +
					'<tr>' +
					'<td class="eia_align_labels"><label for="eia_index">Index</label></td>' +
            		'<td class="eia_popup_spacing"><input type="text" id="eia_index" size="7"/></td>' +
					'</tr><tr>' +
					'<td class="eia_align_labels"><label for="eia_fund">Fund</label></td>' +
					'<td class="eia_popup_spacing"><input type="text" id="eia_fund" size="6"/></td>' +
					'</tr><tr>' +
					'<td class="eia_align_labels"><label for="eia_org">Organization</label></td>' +
					'<td class="eia_popup_spacing"><input type="text" id="eia_org" size="6"/></td>' +
					'</tr><tr>' +
					'<td class="eia_align_labels"><label for="eia_prog">Program</label></td>' +
					'<td class="eia_popup_spacing"><input type="text" id="eia_prog" size="6"></td>' +
					'</tr><tr>' +
					'<td class="eia_align_labels"><label for="eia_title">Description</label></td>' +
					'<td class="eia_popup_spacing"><textarea id="eia_title" cols="30"></textarea></td>' +
					'</tr><tr>' +
					'<td><input type="button" value="Save" id="eia_popup_send" class="eia_buttons_alignment_left "/></td><td><input type="button" value="Cancel" id="eia_popup_close" class="eia_buttons_alignment_right "/></td>' +
					'</tr></table></div><div class="editIndexes_overlay"></div>');
			hidden_index_value = data.index;
			$('#eia_index').val(data.index);
			$('#eia_fund').val(data.fund);
			$('#eia_org').val(data.org);
			$('#eia_prog').val(data.prog);
			$('#eia_title').val(data.title);

	        $(".pop").slideFadeToggle();
	        $('#eia_vendor_name').focus();
	        $('#eia_popup_send').click(function() {
				if($.trim($('#eia_index').val()).length != 7 ||
						$.trim($('#eia_index').val()).substring(0,3) != 'LIB') {
					$.unblockUI();
					$.blockUI({ message: 'Index must be exactly 7 characters and start with "LIB"'});
					setTimeout($.unblockUI, 500);
				} else if($.trim($('#eia_fund').val()).length != 6 ||
						!editIndexes_checkIsNumeric('#eia_fund', 0, 5) ||
						!editIndexes_checkIsAlpha('#eia_fund', 5, 1)) {
					$.unblockUI();
					$.blockUI({ message: 'Fund must be exactly 6 characters, where the first 5 are digits and the last is a letter'});
					setTimeout($.unblockUI, 500);
				} else if($.trim($('#eia_org').val()).length != 6 ||
						!editIndexes_checkIsNumeric('#eia_org', 0, -1)) {
					$.unblockUI();
					$.blockUI({ message: 'Organization must be exactly 6 digits'});
					setTimeout($.unblockUI, 500);
				} else if($.trim($('#eia_prog').val()).length != 6 ||
						!editIndexes_checkIsNumeric('#eia_prog', 0, -1)) {
					$.unblockUI();
					$.blockUI({ message: 'Program must be exactly 6 digits'});
					setTimeout($.unblockUI, 500);
				} else {
					$.ajax({
						url: "/apbatch/servlets/EditIndexes",
						dataType: 'json',
						data: {EDIT_INDEXES_REQUEST:"EDIT",
								hidden_index: hidden_index_value,
								index: $('#eia_index').val(),
								fund: $('#eia_fund').val(),
								org: $('#eia_org').val(),
								prog: $('#eia_prog').val(),
								title: $('#eia_title').val()
						},
						success:editIndexes_updatePopUp,
						error:function (obj,text,error) {
							$.unblockUI();
							$.blockUI({ message: obj.responseText });
							 setTimeout($.unblockUI, 2000);
						}
					});
				}
	        });
			$("#eia_popup_close").click(function() {
			    $(".pop").slideFadeToggle();
			    $("#editIndexes_dialogModal").empty();
			});
		}
		function editIndexes_updatePopUp(data, status) {
			$.unblockUI();
			$('#editIndexes_checkbox_' + data.hidden_index).remove();
			editIndexes_appendInOrder(data);
			$(".pop").slideFadeToggle();
			$("#editIndexes_dialogModal").empty();
		}
		function editIndexes_appendInOrder(data) {
			var length = $('#editIndexes_table_body').children().length;
			if(length != 0) {
				var tr_children = $('#editIndexes_table_body > tr');
				var inserted_value = 0;
				$.each(tr_children, function() {
					/* only insert if it hasn't been done */
					if(inserted_value === 0) {
						var cmp_result = data.index.localeCompare($(this).children('td:nth-child(2)').text());
						if(cmp_result < 0) {
							/* found first position that was less than the name, so insert here */
							$(data.htmlStr).insertBefore($(this));
							inserted_value = 1;
						} else if(cmp_result === 0) {
							/* either it's before or after since index is unique' */
							$(data.htmlStr).insertAfter($(this));
							inserted_value = 1;
						}
					}
				});
				if(inserted_value === 0) {
					$('#editIndexes_table_body').append(data.htmlStr);
				}
			} else {
				$('#editIndexes_table_body').append(data.htmlStr);
			}
		}
		function editIndexes_deleteEntries(data, status) {
			var tmp = $('input[name="editIndexes_checkboxes"]:checked');
			var pDeletedItems = tmp.getValue().split(',');
			var length = tmp.length;  
			for(var x = 0; x < length; ++x) {
				$('#editIndexes_checkbox_' + pDeletedItems[x]).remove();
			}
		}

		function displayProbQueue(data,status){
			
			$.unblockUI();
			sessionData = data;
			initProblem(data);
		}
		 function initProblem(data){
				
				$.unblockUI();
				//$('#filediv').hide();
				//$('#actions').hide();
				//$('#indexdiv').show();
				$("#title").empty();
		    	$("#title").append("Problem Items");
			
				if(data.ProblemTotal === 0){
					//alert("total"+data.total);
					displayEmptyData();
				}else{
					//alert("total"+data.total);
					loadGridProblem(data, initHelpers);
				}
			}
    function initDel(data){
    	$.unblockUI();
			
			//$('#filediv').hide();
			//$('#actions').hide();
			//$('#indexdiv').show();
    	//$("#indexdiv").append("Deleted Items");
    	$("#title").empty();
    	$("#title").append("Deleted Items");
			if(data.delTotal === 0){
				//alert("total"+data.total);
				displayEmptyData();
			}else{
				//alert("total"+data.total);
				loadGridDel(data, initHelpers);
			}
		}
		function init(data){
		
			$.unblockUI();
			//$('#filediv').hide();
			//$('#actions').hide();
			//$('#indexdiv').show();
			
			$("#title").empty();
	    	$("#title").append("File Content");
			if(data.total === 0){
				//alert("total"+data.total);
				displayEmptyData();
			}else{
				//alert("total"+data.total);
				loadGrid(data, initHelpers);
			}
		}
		/** Show message when no data is returned from ajax call **/
		function displayEmptyData(){
			$("#grid").append($("<div id='emptyTable'></div>").html("No data "));
			$.unblockUI(); //unblock the ui
			$('.container').fadeIn('fast'); //show the new data
			
		}
		/** POPULATE HTML GRID **/
		function loadGrid(data, callback){
			//go through each row, add to table
			$.each(data.rows, function(){
				appendRow(this);
			});
			//CALL GRID HELPERS
			if ($.isFunction(callback)) {
				callback.apply(this,[data]);
			}
		}
		
		function loadGridDel(data, callback){
			//go through each row, add to table
			$.each(data.delRows, function(){
				appendRow(this);
			});
			//CALL GRID HELPERS
			if ($.isFunction(callback)) {
				callback.apply(this,[data]);
			}
		}
		
		

		function loadGridProblem(data, callback){
			//go through each row, add to table
			$.each(data.ProblemRows, function(){
				appendRow(this);
			});
			//CALL GRID HELPERS
			if ($.isFunction(callback)) {
				callback.apply(this,[data]);
			}
		}
		/** ADD EACH NEW ROW TO THE TABLE */

		function appendRow(row){
			//alert(row.voucherNo);
			var rows =  $("<tr></tr>");
			var col0 = $("<td></td>");
		    var chkboxVoucher = $('<input type="checkbox" id ="chkDisplayVoucher" value = '+row.voucherNo+'='+row.recType+' />')		
			var col = $("<td></td>");
		    var chkbox = $('<input type="checkbox" id ="chkDisplay" value = '+row.recType+' />')
		    if(row.voucherNo != 'TOTAL') {
		        col0.append(chkboxVoucher);
				col.append(chkbox);
			}
			var col1 =  $("<td></td>").html($.trim(row.voucherNo));
			var col2 =  $("<td></td>").html($.trim(row.recType));
			var col3 =  $("<td></td>").html($.trim(row.fundCode));
			var col4 =  $("<td></td>").html($.trim(row.subfundNo));
			var col5 =  $("<td></td>").html($.trim(row.externalFund));
			var col6 =  $("<td></td>").html($.trim(row.paidDate));
			var col7 =  $("<td></td>").html($.trim(row.invDate));
			var col8 =  $("<td></td>").html($.trim(row.invNo));
			var col9 =  $("<td></td>").html($.trim(row.amount));
			var col10 =  $("<td></td>").html($.trim(row.tax));
			var col11 =  $("<td></td>").html($.trim(row.useTax));
			var col12 =  $("<td></td>").html($.trim(row.ship));
			var col13 =  $("<td></td>").html($.trim(row.discount));
			var col14 =  $("<td></td>").html($.trim(row.listPrice));
			var col15 =  $("<td></td>").html($.trim(row.lien));
			var col16 =  $("<td></td>").html($.trim(row.lienFlag));
			var col17 =  $("<td></td>").html($.trim(row.status));
			var col18 =  $("<td></td>").html($.trim(row.notes));
			var col19 =  $("<td></td>").html($.trim(row.vendorCode));
			//var col20 =  $("<td></td>").html($.trim(row.altVendorCode));
			var col21 =  $("<td></td>").html($.trim(row.vendorName));
			var col22 =  $("<td></td>").html($.trim(row.taxcode));

			//rows.append(col).append(col1).append(col2).append(col3).append(col4).append(col5).append(col6).append(col7).append(col8).append(col9).append(col10).append(col11).append(col12).append(col13).append(col14).append(col15).append(col16).append(col17).append(col18).append(col19).append(col20).append(col21).append(col22);
			//rows.append(col).append(col1).append(col2).append(col3).append(col4).append(col5).append(col6).append(col7).append(col8).append(col9).append(col10).append(col11).append(col12).append(col13).append(col14).append(col15).append(col16).append(col17).append(col18).append(col19).append(col21).append(col22);
			rows.append(col0).append(col).append(col1).append(col2).append(col3).append(col5).append(col6).append(col7).append(col8).append(col9).append(col10).append(col11).append(col12).append(col13).append(col19).append(col21).append(col22);
			$("table.dataGrid").append(rows);
			//==============
		}


		/** INITIALIZE HELPER TABLE GRID FUNCTIONS **/
		function initHelpers(data){
			 //updateDates(data);
			initGrid();
			$.unblockUI(); //unblock the ui
			$('.container').fadeIn('fast'); //show the new data
		}

		/** INITIALIZE GRID CSS **/
		function initGrid(){
			$("table.dataGrid tr:even").addClass("even");
			$("table.dataGrid tr").hover(function () {
		        $(this).addClass("over");
		      }, 
		      function () {
		        $(this).removeClass("over");
		      }
			);	
		}
		
		 function confirmSubmitPending(myString)
		 {
		 var agree=confirm("Are you sure you wish to continue?");
		 if (agree)
		 	{
			$.blockUI({ message: '<img src="images/busy.gif" /> Loading updated table...' });
			
			$.ajax({
					url: "/apbatch/servlets/ModifyQueues", 
					dataType: 'json',
					data: {invoiceArr:myString},
					success:  function(data){
						$("table.dataGrid tbody").empty();
						$("table.dataGrid thead").show();
						//$('.data_container').hide(); 
						sessionData = data;
						$.unblockUI();
						$('#filediv').hide();
						$('#actions').hide();
						$('#indexdiv').show();
						$('#buttonsPending').show();
						init(data);
							},
					error:function (data, status, e){
						$.unblockUI();
						//add error message
						$.blockUI({ message: 'There was an error or timeout when caling ModifyQueues servlet' });
						 setTimeout($.unblockUI, 2000);
					}
				});	
		 	}
		 else
		 {
			 displayPendingQueueConfirmNo(jsonData); 
			 return false ;
		 }
		 	
		 }
		 

		//show the edit window to new patron queue
		function displayEditWindowForProblemQueue(data,status){
			
			var voucher;
			var poNumber;
			var fundCode;
			var extFund;
			var paidDate;
			var invDate;
			var invNo;
			var amount;
			var tax;
			var useTax;
			var ship;
			var discount;
			var vendorCode;
			var vendorName;
			var taxCode;
			var array = null;
		    var newExtFund =null;
		    var newExtFundNew =null;
			voucher = data['basicData'].voucherNo;
			poNumber = data['basicData'].recType;
			fundCode = data['basicData'].fundCode;
			extFund = data['basicData'].externalFund;
			paidDate=data['basicData'].paidDate;
			invDate=data['basicData'].invDate;
			invNo=data['basicData'].invNo;
			amount=data['basicData'].amount;
			tax=data['basicData'].tax;
			useTax=data['basicData'].useTax;
			ship=data['basicData'].ship;
			discount=data['basicData'].discount;
			vendorCode=data['basicData'].vendorCode;
			vendorName=data['basicData'].vendorName;
			taxCode=data['basicData'].taxcode;
			if(extFund !== undefined)
				newExtFund =extFund.replace(" ", "") ;
				else
					newExtFund=extFund;
			if(newExtFund != null)
			newExtFundNew = newExtFund.replace(" ", "") ;
			
			//alert(newExtFund);
			//alert(newExtFund.length);
			
			var html='';
			html+='<html><head><link rel="stylesheet" type="text/css" href="css/popup.css" /></script>';
			html += '<script type="text/javascript" src="js/shared/jquery-1.2.6.pack.js"></script>';
			html += '<script type="text/javascript" src="js/shared/jquery.blockUI.js"></script>';
			html += '<script type="text/javascript" src="js/shared/jquery-ui-1.5.3.packed.js"></script>';
			html += '<script type="text/javascript" src="js/shared/jquery-ui-effects.packed.js"></script>';
			html += '<script type="text/javascript" src="js/shared/hoverIntent.js"></script>';
			html += '<script type="text/javascript" src="js/shared/jquery.field.js"></script>';
			html += '<script type="text/javascript" src="js/shared/Tokenizer.js"></script>';
			html += '<script type="text/javascript" src="js/apbatch.js"></script>';	
			html += '<script type="text/javascript" src="js/shared/loadMenu.js"></script>';
			html += '<script language="Javascript" type="text/javascript">';
			html += 'var recType2= "'+poNumber+'";';
			html += 'var strFundCodeMod = null;';
			html += 'var strExtFundCodeMod = null;';
			html += 'var txtInvDateMod = null;';
			html += 'var txtInvNoMod = null;';
			html += 'var txtAmountMod = null;';
			html += 'var txtVoucherMod = null;';
			html += 'var txtPONumberMod = null;';
			html += 'var txtPaidDateMod = null;';
			html += 'var txtTaxMod = null;';
			html += 'var txtUseTaxMod = null;';
			html += 'var txtShipMod = null;';
			html += 'var txtDiscountMod = null;';
			html += 'var txtVendorCodeMod = null;';
			html += 'var txtVendNameMod = null;';
			html += 'var txtTaxCodeMod = null;';															
			html += 'function CallParentWindowFunction(){ ;';
			html += 'var countS=1;';
			html += 'var recType2= "'+poNumber+'";';
			html += 'var strFundCode = document.getElementById("txtFundCode").value;';
			html += 'var strExtFundCode = document.getElementById("txtExtFund").value;';			
			html += 'var txtInvDate = document.getElementById("txtInvDate").value;';
			html += 'var txtInvNo = document.getElementById("txtInvNo").value;';	
			html += 'var txtAmount = document.getElementById("txtAmount").value;';
			html += 'var txtVoucher = document.getElementById("txtVoucher").value;';	
			html += 'var txtPONumber = document.getElementById("txtPONumber").value;';
			html += 'var txtPaidDate = document.getElementById("txtPaidDate").value;';	
			html += 'var txtTax = document.getElementById("txtTax").value;';
			html += 'var txtUseTax = document.getElementById("txtUseTax").value;';	
			html += 'var txtShip = document.getElementById("txtShip").value;';	
			html += 'var txtDiscount = document.getElementById("txtDiscount").value;';
			html += 'var txtVendorCode = document.getElementById("txtVendorCode").value;';	
			html += 'var txtVendName = document.getElementById("txtVendName").value;';
			html += 'var txtTaxCode = document.getElementById("txtTaxCode").value;';																	
			html += 'if((strFundCode.length == 0) || (strExtFundCode.length==0)){';
			html += 'alert("Please enter both Fund Code and External Fund Code to proceed.");';
			html += 'return false;';			
			html += '}';
			html += 'function processStr(str){ ;';
			html += 'if (str != null) {';			
			html += 'return str.replace(/^\s+/g,"").replace(/\s+$/g,"");';	
			html += '} else {return str;}';			
			html += '}';			
			html += 'strFundCodeMod = processStr(strFundCode);';
			html += 'strExtFundCodeMod = processStr(strExtFundCode);';
			html += 'txtInvDateMod = processStr(txtInvDate);';
			html += 'txtInvNoMod = processStr(txtInvNo);';
			html += 'txtAmountMod = processStr(txtAmount);';
			html += 'txtVoucherMod = processStr(txtVoucher);';
			html += 'txtPONumberMod = processStr(txtPONumber);';
			html += 'txtPaidDateMod = processStr(txtPaidDate);';
			html += 'txtTaxMod = processStr(txtTax);';
			html += 'txtUseTaxMod = processStr(txtUseTax);';
			html += 'txtShipMod = processStr(txtShip);';
			html += 'txtDiscountMod = processStr(txtDiscount);';
			html += 'txtVendorCodeMod = processStr(txtVendorCode);';
			html += 'txtVendNameMod = processStr(txtVendName);';
			html += 'txtTaxCodeMod = processStr(txtTaxCode);';
															
			//html += 'var strExtFundCodeMod = strExtFundCode.replace(/^\s+/g,"").replace(/\s+$/g,"");';
			//html += 'alert(strnote.length+","+strnoteMod.length);';
			//html+=  'var flag = strExtFundCodeMod.match("LIB");';
			html += 'var index1 = strExtFundCodeMod.indexOf("LIB");';
			//html+= 'if (flag == null){ alert("Please enter a valid External Fund Code!");return false;}';
			html+= 'if (index1 == -1){ alert("Please enter a valid External Fund!");return false;}';
			html+=  'var len = strFundCodeMod.length;';
			html+= 'if (len >5) {alert("Fund Code should be less than 6 characters!");return false;}';
			var ajaxcall = '$.ajax({url: "/apbatch/servlets/CheckExtFundCode", dataType: "json",data:{extFundCode:strExtFundCodeMod},success:test,error:function (data, status, e){$.unblockUI();$.blockUI({ message: "There was an error or timeout when calling CheckExtFundCode servlet" }); setTimeout($.unblockUI, 2000);}});';
			html += ajaxcall;
			//html+= 'alert(countS);';
			//html+= 'if (countS == 0){ alert("The external fund code entered is not valid!");return false;}';
		//	html+= '}';
			//html += 'if(strFundCodeMod.length < 1) {alert("Enter Fund Code!");return false;}';
			//html +=  'window.opener.saveAllDataFromProbQueue(recType2,strFundCodeMod,strExtFundCodeMod,txtInvDateMod,txtInvNoMod,txtAmountMod,txtVoucherMod,txtPONumberMod,txtPaidDateMod,txtTaxMod,txtUseTaxMod,txtShipMod,txtDiscountMod,txtVendorCodeMod,txtVendNameMod,txtTaxCodeMod);window.close();return true;}';
			html += '}';
			html+= 'function test(data,status){';
			html+= 'if(data.TOT === 0){';
			html += 'alert("The external fund entered is not valid!");';
			html += '}';
			html += 'else{'
				html +=  'window.opener.saveAllDataFromProbQueue(recType2,strFundCodeMod,strExtFundCodeMod,txtInvDateMod,txtInvNoMod,txtAmountMod,txtVoucherMod,txtPONumberMod,txtPaidDateMod,txtTaxMod,txtUseTaxMod,txtShipMod,txtDiscountMod,txtVendorCodeMod,txtVendNameMod,txtTaxCodeMod);window.close();return true;';
			html += '}}';
			html += '</script>';
			
			
			html += '<TITLE>Edit Problem Queue Record</TITLE></head><body><div>';
			html+= '<fieldset><legend>Edit Problem Queue data </legend>';
			html+= '<table border="0"> <tr><td>Voucher No:</td><td><input type="text" id="txtVoucher" size="30" value= '+voucher+'> </td></tr>';
			html+='<tr><td>PO Number:</td><td><input type="text" id="txtPONumber" size="30" value= '+poNumber+'></td></tr>';
			html+='<tr><td>Fund Code:</td><td><input type="text" id="txtFundCode" size="30" value= '+fundCode+'></td> </tr>';
			html+='<tr><td>External Fund:</td><td><input type="text" id="txtExtFund" size="50" value= '+newExtFundNew+' ></td></tr>';
			html+='<tr><td>Paid Date:</td><td><input type="text" id="txtPaidDate" size="30" value= '+paidDate+'></td></tr>';
			html+='<tr><td>Invoice Date:</td><td><input type="text" id="txtInvDate" size="30" value= '+invDate+'></td></tr>';
			html+='<tr><td>Invoice No:</td><td><input type="text" id="txtInvNo" size="30" value= '+invNo+'></td></tr>';
			html+='<tr><td>Amount:</td><td><input type="text" id="txtAmount" size="30" value= '+amount+'></td></tr>';
			html+='<tr><td>Tax:</td><td><input type="text" id="txtTax" size="30" value= '+tax+'></td></tr>';
			html+='<tr><td>Use Tax:</td><td><input type="text" id="txtUseTax" size="30" value= '+useTax+'></td></tr>';
			html+='<tr><td>Ship:</td><td><input type="text" id="txtShip" size="30" value= '+ship+'></td></tr>';
			html+='<tr><td>Discount:</td><td><input type="text" id="txtDiscount" size="30" value= '+discount+'></td></tr>';
			html+='<tr><td>Vendor Code:</td><td><input type="text" id="txtVendorCode" size="30" value= '+vendorCode+'></td></tr>';
			html+='<tr><td>Vendor Name:</td><td><input type="text" id="txtVendName" size="30" value= '+vendorName+'></td></tr>';
			html+='<tr><td>Tax Code:</td><td><input type="text" id="txtTaxCode" size="30" value= '+taxCode+'></td></tr>';
			html+='<td><input type="button" value="Ok" onclick="CallParentWindowFunction()"></td> ';
			html+='<td><input type="button" value="Cancel" onclick="window.close()"></td></tr> </table>';
					
			html+='<br></div>';
			html += '</fieldset>';
			
		    html+='</body></html>';
			win2 = window.open("", "window2", "width=780,height=800,scrollbars=yes "); 
			var tmp = win2.document;
			tmp.write(html);
			tmp.close();
			win2.focus();
			document.body.style.cursor = 'default'; 
		}

	});	//end  $(function(){
 function saveDataFromProbQueue(recType,fundCode,extFundCode)
 {
	 $.ajax({
			url: "/apbatch/servlets/EditProblemQueueData", 
			dataType: 'json',
			data: {recType:recType,fundCode:fundCode,extFundCode:extFundCode},
			success: displayProblemQueueAfterEdit,
			error:function (data, status, e){
				$.unblockUI();
				$.blockUI({ message: 'There was an error or timeout when calling EditProblemQueueData servlet' });
				 setTimeout($.unblockUI, 2000);
			}
		});	
 }

 function saveAllDataFromProbQueue(recType,fundCode,extFundCode,txtInvDate,txtInvNo,txtAmount,txtVoucher,
        txtPONumber,txtPaidDate,txtTax,txtUseTax,txtShip,txtDiscount,txtVendorCode,txtVendName,txtTaxCode)
 {
	 $.ajax({
			url: "/apbatch/servlets/EditProblemQueueData", 
			dataType: 'json',
			data: {recType:recType,fundCode:fundCode,extFundCode:extFundCode,txtInvDate:txtInvDate,txtInvNo:txtInvNo,txtAmount:txtAmount,
			txtVoucher:txtVoucher,txtPONumber:txtPONumber,txtPaidDate:txtPaidDate,txtTax:txtTax,txtUseTax:txtUseTax,txtShip:txtShip,
			txtDiscount:txtDiscount,txtVendorCode:txtVendorCode,txtVendName:txtVendName,txtTaxCode:txtTaxCode},
			success: displayProblemQueueAfterEdit,
			error:function (data, status, e){
				$.unblockUI();
				$.blockUI({ message: 'There was an error or timeout when calling EditProblemQueueData servlet' });
				 setTimeout($.unblockUI, 2000);
			}
		});	
 }
  
 function displayProblemQueueAfterEdit(data,status){
	 
	    sessionData = data;
	    $('#emptyTable').remove();
		$("table.dataGrid tbody").empty();
		$("table.dataGrid thead").show();
		//$("table.dataGrid.thead").show();
		//$('.data_container').hide(); 
		$('#filediv').hide();
		$('#actions').hide();
		$('#buttonsPending').hide();
		$('#indexdiv').show();
		$('#buttonsProblem').show();
		initProblem(data);
 }	
 function initProblem(data){
		
		$.unblockUI();
		//$('#filediv').hide();
		//$('#actions').hide();
		//$('#indexdiv').show();
		$("#title").empty();
 	$("#title").append("Problem Items");
	
		if(data.ProblemTotal === 0){
			//alert("problem total"+data.total);
			displayEmptyData();
		}else{
			//alert("load grid problem total"+data.total);
			loadGridProblem(data, initHelpers);
		}
	}
 
 function displayEmptyData(){
		$("#grid").append($("<div id='emptyTable'></div>").html("No data "));
		$.unblockUI(); //unblock the ui
		$('.container').fadeIn('fast'); //show the new data
		
	}
 function loadGridProblem(data, callback){
		//go through each row, add to table
		$.each(data.ProblemRows, function(){
			appendRow(this);
		});
		//CALL GRID HELPERS
		if ($.isFunction(callback)) {
			callback.apply(this,[data]);
		}
	}
 
 function appendRow(row){
		//alert(row.voucherNo);
		var rows =  $("<tr></tr>");
		var col0 = $("<td></td>");
		var chkboxVoucher = $('<input type="checkbox" id ="chkDisplayVoucher" value = '+row.voucherNo+'='+row.recType+' />')		
		col0.append(chkboxVoucher);
		var col = $("<td></td>");
	    var chkbox = $('<input type="checkbox" id ="chkDisplay" value = '+row.recType+' />')
		col.append(chkbox);
		var col1 =  $("<td></td>").html($.trim(row.voucherNo));
		var col2 =  $("<td></td>").html($.trim(row.recType));
		var col3 =  $("<td></td>").html($.trim(row.fundCode));
		var col4 =  $("<td></td>").html($.trim(row.subfundNo));
		var col5 =  $("<td></td>").html($.trim(row.externalFund));
		var col6 =  $("<td></td>").html($.trim(row.paidDate));
		var col7 =  $("<td></td>").html($.trim(row.invDate));
		var col8 =  $("<td></td>").html($.trim(row.invNo));
		var col9 =  $("<td></td>").html($.trim(row.amount));
		var col10 =  $("<td></td>").html($.trim(row.tax));
		var col11 =  $("<td></td>").html($.trim(row.useTax));
		var col12 =  $("<td></td>").html($.trim(row.ship));
		var col13 =  $("<td></td>").html($.trim(row.discount));
		var col14 =  $("<td></td>").html($.trim(row.listPrice));
		var col15 =  $("<td></td>").html($.trim(row.lien));
		var col16 =  $("<td></td>").html($.trim(row.lienFlag));
		var col17 =  $("<td></td>").html($.trim(row.status));
		var col18 =  $("<td></td>").html($.trim(row.notes));
		var col19 =  $("<td></td>").html($.trim(row.vendorCode));
		//var col20 =  $("<td></td>").html($.trim(row.altVendorCode));
		var col21 =  $("<td></td>").html($.trim(row.vendorName));
		var col22 =  $("<td></td>").html($.trim(row.taxcode));
				
		//rows.append(col).append(col1).append(col2).append(col3).append(col4).append(col5).append(col6).append(col7).append(col8).append(col9).append(col10).append(col11).append(col12).append(col13).append(col14).append(col15).append(col16).append(col17).append(col18).append(col19).append(col20).append(col21).append(col22);
		//rows.append(col).append(col1).append(col2).append(col3).append(col4).append(col5).append(col6).append(col7).append(col8).append(col9).append(col10).append(col11).append(col12).append(col13).append(col14).append(col15).append(col16).append(col17).append(col18).append(col19).append(col21).append(col22);
		rows.append(col0).append(col).append(col1).append(col2).append(col3).append(col5).append(col6).append(col7).append(col8).append(col9).append(col10).append(col11).append(col12).append(col13).append(col19).append(col21).append(col22);
		$("table.dataGrid").append(rows);
		//==============
	}
 
	function initHelpers(data){
		 //updateDates(data);
		initGrid();
		$.unblockUI(); //unblock the ui
		$('.container').fadeIn('fast'); //show the new data
	}
	function initGrid(){
		$("table.dataGrid tr:even").addClass("even");
		$("table.dataGrid tr").hover(function () {
	        $(this).addClass("over");
	      }, 
	      function () {
	        $(this).removeClass("over");
	      }
		);	
	}
	
function displaySendFileLog(data,status){
	$.unblockUI();
	
	if(data.success === "true")
	{
		jsonData = data;
		//jsonData['problem'] == null;
		alert("successfully transmitted!");
		//jsonData['pending'] = data['pendingQueue'];
		//jsonData['pendingTotal'] = data.pendingTotal;
		/*$('#buttonsPending').show();
		$('#buttonsNewPatron').hide();
		$('#buttonsProblem').hide();
		$('#searchDiv').hide();*/
		$('#sessionDiv').hide();
		//$('#indexdiv').show();
		 
		$('#actions').hide();
		//displayQueue("Pending Queue",data['pending'],data.pendingTotal);
		$('.container').fadeIn('fast'); //show the new data
	}
	else{
		alert(data.errorMsg);
	
    }
}

function displayBuildFile(data,status){
	$.unblockUI();
    if(data.indexOf("Error") > -1) {
    	alert(data);		
    } else {
    	window.open("/apbatch/servlets/ProcessOutputData");
    }
}
	
function getPassword(username,password) {
	

    
   // alert(username);
	// alert(password);	
   
	if(username==null || username=="")
		 {alert("You need to enter username!");}
	 else if (password==null || password=="")
		 {alert("You need to enter password!");}

	 else
	  {
		 $("#results").empty();
		
	 $.blockUI({ message: '<img src="images/busy.gif" /> Sending Data...' });
	 $.ajax({
			url: "/apbatch/servlets/SendOutputFiles", 
			dataType: 'json',
			data:{username:username,password:password},
			success: displaySendFileLog,
			error:function (data, status, e){
				$.unblockUI();
				//add error message
				$.blockUI({ message: 'There was an error or timeout when caling SendOutputFiles servlet' + data +  status + e});
				 setTimeout($.unblockUI, 2000);
			}
		});	
	  }
  
		   
		}	
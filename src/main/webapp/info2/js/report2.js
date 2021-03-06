$(function(){
	function encryptByDES(message, key) {    
        var keyHex = CryptoJS.enc.Utf8.parse(key);  
        var encrypted = CryptoJS.DES.encrypt(message, keyHex, {    
       		mode: CryptoJS.mode.ECB,    
        	padding: CryptoJS.pad.Pkcs7    
        });   
        return encrypted.toString();    
  	}
	var merchantName=localStorage.getItem('merchantName');
//	var merchantType=localStorage.getItem('merchantType');
//	var merchantBillName=localStorage.getItem('merchantBillName');
	var merchantPersonName=localStorage.getItem('merchantPersonName');
//	var merchantPersonPhone=localStorage.getItem('merchantPersonPhone');
	var merchantPersonEmail=localStorage.getItem('merchantPersonEmail');
//	var businessLicense=localStorage.getItem('businessLicense');
//	var legalPersonName=localStorage.getItem('legalPersonName');
	var legalPersonID=localStorage.getItem('legalPersonID');
	var installProvince=localStorage.getItem('installProvince');
	var installCity=localStorage.getItem('installCity');
	var installCounty=localStorage.getItem('installCounty');
	var operateAddress=localStorage.getItem('operateAddress');
	var provid = localStorage.getItem("provid");
	var loginID = localStorage.getItem("loginID");
	$('#last').click(function(){
		sessionStorage.setItem('installProvince',installProvince);
		sessionStorage.setItem('installCity',installCity);
		sessionStorage.setItem('installCounty',installCounty);
		sessionStorage.setItem("provid",provid);
		window.history.go(-1);
		
	})
	
	$("#submit").click(function(){
		var arr=new Array('开户名称','开户账号','开户银行总行','开户银行支行','联行号','结算人信用卡');
		for (var i=0;i<arr.length;i++) {
			if($('.required').eq(i).val()==''){
				alert(arr[i]+'为空，请返回填写');
				return false;
			}
		}
		if($('#loc_province').val()==""){
			alert('请选择开户行省份');
			return false;
		}
		if($('#loc_city').val()==""){
			alert('请选择开户行城市');
			return false;
		}
		var accountNo=$('#accountNo').val();
		var bankName=$('#bankName').val();
		var bankBranch=$('#bankBranch').val();
		var bankCode=$('#bankCode').val();
		var creditCardNo=$('#creditCardNo').val();
		var bankProv=$('#loc_province').select2('data').text;
		var bankCity=$('#loc_city').select2('data').text;
		

		
		var jsondata="{'accountNo':'"+accountNo+"','bankBranch'" + ":'"+bankBranch+"','bankCity':'"+bankCity+"','bankCode':'"+bankCode+"','bankName':'"+bankName+"','bankProv':'"+bankProv+"',"
				+"'creditCardNo':'"+creditCardNo+"','installCity':'"+installCity+"','installCounty':'"+installCounty+"','installProvince':'"+installProvince+"','legalPersonID':'"
				+ legalPersonID+"','merchantName':'"+merchantName+"','merchantPersonEmail':'"+merchantPersonEmail+"','merchantPersonName':'"
				+ merchantPersonName+"','operateAddress':'"+operateAddress+"','loginID': '"+ loginID +"'}";
		$.ajax({
			type:"post",
			url:"/appserver/in",
			dataType:"json",
			cache: false,
			data:{
				data:jsondata
			},
			success:function(repdata){
				if(repdata.respCode=="00"){
					alert('提交成功');
					location.href="http://www.baidu.com";
					JSInterface.changeActivity();
				}else{
					alert(repdata.respMsg);
				}
			},
			error:function(XMLHttpRequest, textStatus, errorThrown){
				alert("服务异常!");
			}
		});
	})
	
})

$(function(){
	var loginID = localStorage.getItem("loginID");
	$.ajax({
		type:"post",
		url:"/appserver/getuserbankcard",
		data:{
			loginID:loginID
		},
		success:function(repdata){
			$("#accountName").val(repdata.AccountName);
			$("#accountNo").val(repdata.AccountNo);
			$("#bankType").val(repdata.SettleBankType);
			$("#bankName").val(repdata.BankName);
			$("#bankBranch").val(repdata.BankBranch);
			$("#bankCode").val(repdata.BankCode);
			$("#creditCardNo").val(repdata.SettleCreditCard);
		}
	});
})


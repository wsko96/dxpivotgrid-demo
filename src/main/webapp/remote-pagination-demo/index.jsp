<%@ page language="java" %>
<%@ page import="org.apache.commons.lang3.math.NumberUtils" %>

<%
int offset = NumberUtils.toInt(request.getParameter("offset"), 0);
int limit = NumberUtils.toInt(request.getParameter("limit"), 7);
%>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>DevExtreme Demo</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script>window.jQuery || document.write(decodeURIComponent('%3Cscript src="js/jquery.min.js"%3E%3C/script%3E'))</script>
    <link rel="stylesheet" type="text/css" href="https://cdn3.devexpress.com/jslib/18.2.7/css/dx.common.css" />
    <link rel="stylesheet" type="text/css" href="https://cdn3.devexpress.com/jslib/18.2.7/css/dx.light.css" />
    <script src="https://cdn3.devexpress.com/jslib/18.2.7/js/dx.all.js"></script>
    <!--script src="data.js"></script-->
    <link rel="stylesheet" type="text/css" href="styles.css" />
</head>
<body class="dx-viewport">
    <div class="demo-container">
        <div id="pivotgrid-demo">
            <div id="pivotgrid-chart"></div>
            <div id="pivotgrid"></div>
        </div>
    </div>

    <script>
        var pivotGridPaging = { offset: <%=offset%>, limit: <%=limit%>, rowGroups: [ { selector: 'region' }, { selector: 'city' } ] };
    </script>
    <script src="index.js"></script>

</body>
</html>

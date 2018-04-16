<%-- 
    Document   : menu
    Created on : 13-nov-2017, 10:13:52
    Author     : Norhan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<!DOCTYPE html>
<html>
    <head>
        <%@ include file="infouser.jsp" %>
        <script>
            function templates(){
                var id = $( "#selectyear option:selected" ).val();
                $.ajax({
                    type: "POST",
                    url: "menu/temp.htm?id=" + id,
                    data: id,
                    dataType: 'text',
                    success: function (data) {
                        var tmps = JSON.parse(data);
                        $('#selecttemplate').empty();
                        for(var t in tmps){
                            $('#selecttemplate').append("<option value='"+tmps[t].id+"-"+tmps[t].rows+
                                    "-"+tmps[t].cols+"'>"+tmps[t].name+"</option>");
                        }
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        console.log(xhr.status);
                        console.log(xhr.responseText);
                        console.log(thrownError);
                    }
                });
            }
            
            function hideroomsgroup(){
                var selectval = $('#roomsmod').val();
                if(selectval === 2 || selectval === 3){
                    $('#grouprooms').show();
                }else{
                    $('#grouprooms').hide();
                }
            }
            /*private int id;
    private int cols;
    private int rows;
    private String name;*/
        </script>
    </head>
    <body>
        <div class="col-xs-12">
            <form:form action="menu/create.htm" method="POST">
                <div class="col-xs-3">
                    <h3>Select Year</h3>
                    <select id="selectyear" name="yearid" onchange="templates()">
                        <option></option>
                        <c:forEach var="year" items="${years}">
                            <option value="${year.x}">${year.y}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-xs-3">
                    <h3>Select Template</h3>
                    <select name="templateInfo" id="selecttemplate">
                    </select>
                </div>
                <div class="col-xs-3">
                    <h3>Select rooms schedule mode</h3>
                    <select id="roomsmode"name="rooms" onchange="hideroomsgroup()">
                        <option value="0">disabled</option>
                        <option value="1">only courses with room restrictions</option>
                        <option value="2">only default school user defined</option>
                        <option value="3">both (courses and default)</option>
                    </select>
                    <select id="grouprooms" name="groupofrooms">
                        <option value="rooms01">rooms 01</option>
                        <option value="rooms02">rooms 02</option>
                        <option value="rooms03">rooms 03</option>
                        <option value="rooms04">rooms 04</option>
                    </select>
                </div>
                    
                <div class="col-xs-3">
                    <h3>Create Schedule</h3>
                    <input class="btn btn-primary btn-lg" type="submit" name="Submit" value="Create">
                </div>
            </form:form>
        </div>
    </body>
</html>

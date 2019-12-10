<%@page import="ds.gae.view.JSPSite"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<% session.setAttribute("currentPage", JSPSite.CONFIRM_QUOTES_RESPONSE); %>
<% String renter = (String) session.getAttribute("renter"); %>

<%@include file="_header.jsp"%>

<div class="frameDiv" style="margin: 150px 150px;">
	<h2>Reply</h2>
	<div class="group">
		<p>
			Hello <%=renter%>, <br> <br>
			Your quotes are being processed. You will be notified of the result by email in, at most, a couple of days.
		</p>
	</div>
</div>

<%@include file="_footer.jsp"%>

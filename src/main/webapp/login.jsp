<%@page import="ds.gae.view.JSPSite"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<% session.setAttribute("currentPage", JSPSite.LOGIN); %>
<% String renter = null; %>

<%@include file="_header.jsp"%>

<div class="frameDiv" style="margin: 150px 280px;">
	<h2>Login</h2>
	<p>Please enter your email below. This will be used for feedback when confirming quotes</p>
	<form method="POST" action="/login">
		<div class="group">
			<div class="form">
				<span><input type="text" name="username" value="TestUser" size="10">@gmail.com</span>
			</div>
			<div class="formsubmit">
				<input type="submit" value="Login" />
			</div>
		</div>
	</form>
</div>

<%@include file="_footer.jsp"%>

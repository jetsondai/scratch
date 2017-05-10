<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="row">
	<div class="col-md-6">
		<label>过滤：</label>
		<div class="btn-group">
			<button type="button" class="btn btn-default dropdown-toggle"
				data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
				类型 <span class="caret"></span>
			</button>
			<ul class="dropdown-menu">
				<li><a href="focus?focus=${focus}">全部</a></li>
				<li role="separator" class="divider"></li>
				<c:forEach var="animeType" items="${animeTypes.entrySet() }">
					<li><a href="focus?type=${animeType.key }&focus=${focus}">${animeType.value }</a></li>
				</c:forEach>
			</ul>
		</div>
		<c:choose>
			<c:when test="${type == null }">全部</c:when>
			<c:otherwise>${animeTypes[type] }</c:otherwise>
		</c:choose>
		<div class="btn-group">
			<button type="button" class="btn btn-default dropdown-toggle"
				data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
				订阅 <span class="caret"></span>
			</button>
			<ul class="dropdown-menu">
				<li><a href="focus?type=${type }">全部</a></li>
				<li role="separator" class="divider"></li>
				<li><a href="focus?type=${type }&focus=1">订阅中</a></li>
				<li><a href="focus?type=${type }&focus=0">未订阅</a></li>
			</ul>
		</div>
		<c:choose>
			<c:when test="${focus == null }">全部</c:when>
			<c:when test="${focus == 1 }">订阅中</c:when>
			<c:otherwise>未订阅</c:otherwise>
		</c:choose>
	</div>
</div>
<br />
<div class="row anime-focus">
	<c:forEach var="anime" items="${animeList }">
		<div class="col-md-3 col-xs-4">
			<div class="thumbnail">
				<div class="video-img">
					<img src="${anime.key.pic }" class="img-rounded" />
				</div>
				<div class="video-desc">${anime.key.name }</div>
				<div>
					<c:choose>
						<c:when test="${anime.value == 0 }">
							<form action="focus/add" method="post">
								<input name="animeId" type="hidden" value="${anime.key.id }" />
								<button class="btn btn-success">订阅</button>
							</form>
						</c:when>
						<c:otherwise>
							<form action="focus/delete" method="post">
								<input name="animeId" type="hidden" value="${anime.key.id }" />
								<button class="btn btn-primary">取消订阅</button>
							</form>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</div>
	</c:forEach>
</div>
## A/B load balancing proxy CT3

This is a proxy load balancer created to facilitate A/B testing. This means that certain user sessions are assigned to various servers that run a variation of the application in question.  Imagine you want to release a feature to only certain users.  You can create a branch, deploy it to a server and direct x amount of traffic towards that server. You can do the same with multiple branch deployments.

The basic functionality is that you can direct a user to a configured set of servers based on server selection strategy.  The user can interact with the web site being proxied and for the duration of the web session the website you're forwarded to is sticky, though the requests are redirected to same website.  If you close and open the browser (though destroying the session), the proxy selects another server from the pool.

You're tasked to debug, refactor and add some functionality to this app in order to demonstrate your abilities within a real world environment.


### Task 1
When a new user (session) is created during the first visit, the session cookie that we assign does not exist.  We assign this cookie on the first request by randomly selecting a server from the pool and that cookie is used for the duration of the session.  A concurrency bug exists there in Chrome and possibly other browsers.  

When the first request for say index.html comes in, the proxy selects a server, uses it to proxy the request and then before streaming the response, assigns a cookie (cookie name is in the config file).  When the browser renders the response, resources on that page (i.e. javascript, css, images) are requested concurrently, but these concurrent requests do not have the cookie set as they appear to the proxy as new requests.  The proxy then goes through the same process of picking a server (which in many cases isn't the same as original request) and proxying the response to that server as well as assigning a cookie with a different server name.  This basically yields the initial session request non-deterministic due to the race conditions imposed by concurrent resource requests.

You are to find and implement a solution to this problem.


### Task 2

Currently the strategy for selecting a server/application is at random from the server pool.  We'd like for you to implement a percentage based selection.  The config file should allow you to configure a percent of sessions that will be sent to each site.  You would then implement the strategy and write tests for it.


### Task 3

Most proxy servers support X headers in order to look more transparent to the backend application and give access to the information about the initial request.

Please implement support for X-Forward-For, X-Forward-Proto, X-Forward-Host headers.

Also implement a custom X-For-App header that will forward the selected server name to the backend application.


### Task 4

If you find anything in the application that can be more concise, cleaner, or benefit from refactoring, please do that.  Also, if you see a way to improve the unit tests, please do so.  This includes the existing functionality as well as anything you add/modify.



## Instructions

1. Please fork the repository to your own in github.
2. Perform the work above
3. Submit a pull request
4. In an in-person session, we'll discuss your changes looking at the pull request (feel free to annotate the pull request).  Be prepared present and discuss your changes/additions to the interview team (2 people max).

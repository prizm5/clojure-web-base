

var packages = [
	{name: "dojo", location: "./" },
	{name: "dijit", location: "../dijit" },
	{name: "dojox", location: "../dojox" },
	{name: "gridx", location: "../gridx" },
	{name: "idx", location: "../../idx" },
	{name: "docUI", location: "../../docUI" }

];

var dojoConfig = {
	async: true,
	packages: packages,
	locale: lang,
	gfxRenderer: 'svg,silverlight,vml'
};
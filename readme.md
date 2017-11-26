# Local Investor Backend
Our idea based on peer-to-peer landing, but in a more personalized way. We thought about how we can make finance flow more interactive and relatable to customers. So there is an idea about using information about recent purchases and interest based on social media.

## Deploy steps
1) Clone repo. 
2) Create new Project on Google Cloud AppEngine or use existing one.
3) Edit  `build.gradle`. Set `appengine.deploy.project` property as your Project Id and `app engine.deploy.version` to preferred version number.
4) Edit `src/main/webapp/WEB-INF/appengine-web.xml` - set `application` and `version`to the same values as in previous step. Set `service` to preferred AppEngine service name, or set to `default`.
5) Install Google Cloud SDK. Execute `gcloud init`.
6) Switch to repo directory. Execute `gradle appengineDeploy`.
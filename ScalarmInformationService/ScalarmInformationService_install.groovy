import org.cloudifysource.dsl.utils.ServiceUtils;

evaluate(new File("Tools.groovy")) 
def tools = new Tools()

// ---

tools.installCurl()

if (!tools.isRubyValid()) tools.installRvmRuby()

def configFiles = ['secrets.yml', 'scalarm-cert.pem', 'scalarm-cert-key.pem', 'thin.yml']

new AntBuilder().sequential {
    ServiceUtils.getDownloadUtil().get("${tools.config.downloadPath}", "${tools.installDir}/archive.zip", true)
    unzip(src: "${tools.installDir}/archive.zip", dest: tools.installDir, overwrite: true)
    move(file: "${tools.installDir}/${tools.config.serviceName}-${tools.config.scalarmTag}", tofile: tools.serviceDir)  
    
    configFiles.each() { copy(file: it, todir: tools.serviceConfigDir) }
}

tools.command('bundle install', tools.serviceDir)
tools.command('rake db:migrate', tools.serviceDir, ['RAILS_ENV': 'production'])
tools.command('rake db:migrate', tools.serviceDir)


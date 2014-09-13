import org.cloudifysource.dsl.utils.ServiceUtils;

evaluate(new File("Tools.groovy")) 
def tools = new Tools()

// ---

if (!tools.isRubyValid()) tools.installRvmRuby()

def configFiles = ['secrets.yml', 'scalarm-cert.pem', 'scalarm-cert-key.pem', 'thin.yml']

new AntBuilder().sequential {
    ServiceUtils.getDownloadUtil().get("${tools.config.downloadPath}", "${tools.installDir}/master.zip", true)
    unzip(src: "${tools.installDir}/master.zip", dest: "${tools.installDir}", overwrite: true)
    move(file: "${tools.installDir}/${tools.config.serviceName}-master", tofile: "${tools.serviceDir}")
    
    configFiles.each() { copy(file: it, todir: tools.serviceConfigDir) }
}

tools.command('bundle install', tools.serviceDir)
tools.command('RAILS_ENV=production rake db:migrate', tools.serviceDir)
tools.command('rake db:migrate', tools.serviceDir)

// ---



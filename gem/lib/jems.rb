require 'jems_jars'
module Jems

  def self.jems_class_loader
    @class_loader ||= com.github.jrubygradle.jem.JemsClassLoader.create
  end

  def self.activate( gem )
    gemspec_raw = jems_class_loader.add_jem( gem )
    gemspec = eval( gemspec_raw.to_ruby )

    return false if Gem.loaded_specs[ gemspec.name ]

    gem_name = 'gems/' + gemspec.full_name + '/.jrubydir'
    url = jems_class_loader.get_resource( gem_name )
    base = "uri:#{url.to_s.sub(/.jrubydir$/, '')}"

    added = false
    gemspec.require_paths.each do |path|
      fullpath = File.join( base, path )
      if File.directory?(fullpath) and not $LOAD_PATH.member?(fullpath)
        # TODO rubygems insert before 'site_ruby' entry of the LOAD_PATH
        $LOAD_PATH.unshift fullpath
        added = true
      end
    end
    # TODO check if gemspec.activate does the job better
    if added
      Gem.loaded_specs[ gemspec.name ] = gemspec
    end
    added
  end

end

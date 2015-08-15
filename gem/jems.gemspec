#-*- mode: ruby -*-

Gem::Specification.new do |s|
  s.name = 'jems'
  s.version = '0.1.6'
  s.authors = ['Christian Meier']
  s.summary = 'JRuby Jems'
  s.files = ['jems.gemspec', 'lib/jems.rb']
  s.platform = 'java'
  s.requirements << "jar com.github.jrubygradle:jem, #{s.version}"
  s.add_runtime_dependency 'jar-dependencies', '~> 0.1.15'
end

# vim: syntax=Ruby

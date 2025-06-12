import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button.jsx';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card.jsx';
import { Badge } from '@/components/ui/badge.jsx';
import { 
  ShoppingCart, 
  Users, 
  Shield, 
  Database, 
  Code, 
  Truck, 
  Star,
  CheckCircle,
  ArrowRight,
  Trophy,
  Lightbulb,
  Cog,
  ChevronDown,
  Menu,
  X
} from 'lucide-react';
import './App.css';

function App() {
  const [currentSection, setCurrentSection] = useState('hero');
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const scrollToSection = (sectionId) => {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
      setCurrentSection(sectionId);
      setIsMenuOpen(false);
    }
  };

  useEffect(() => {
    const handleScroll = () => {
      const sections = ['hero', 'overview', 'architecture', 'users', 'features', 'database', 'conclusion'];
      const scrollPosition = window.scrollY + 100;

      for (const section of sections) {
        const element = document.getElementById(section);
        if (element && scrollPosition >= element.offsetTop && scrollPosition < element.offsetTop + element.offsetHeight) {
          setCurrentSection(section);
          break;
        }
      }
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navigationItems = [
    { id: 'hero', label: 'Accueil' },
    { id: 'overview', label: 'Aperçu' },
    { id: 'architecture', label: 'Architecture' },
    { id: 'users', label: 'Utilisateurs' },
    { id: 'features', label: 'Fonctionnalités' },
    { id: 'database', label: 'Base de Données' },
    { id: 'conclusion', label: 'Conclusion' }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">
      {/* Navigation */}
      <nav className="fixed top-0 left-0 right-0 bg-white/90 backdrop-blur-md border-b border-gray-200 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-2">
              <ShoppingCart className="h-8 w-8 text-blue-600" />
              <span className="text-xl font-bold text-gray-900">KURESEL</span>
            </div>
            
            {/* Desktop Navigation */}
            <div className="hidden md:flex space-x-8">
              {navigationItems.map((item) => (
                <button
                  key={item.id}
                  onClick={() => scrollToSection(item.id)}
                  className={`text-sm font-medium transition-colors hover:text-blue-600 ${
                    currentSection === item.id ? 'text-blue-600' : 'text-gray-700'
                  }`}
                >
                  {item.label}
                </button>
              ))}
            </div>

            {/* Mobile Menu Button */}
            <div className="md:hidden">
              <button
                onClick={() => setIsMenuOpen(!isMenuOpen)}
                className="text-gray-700 hover:text-blue-600"
              >
                {isMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
              </button>
            </div>
          </div>

          {/* Mobile Navigation */}
          {isMenuOpen && (
            <div className="md:hidden py-4 border-t border-gray-200">
              {navigationItems.map((item) => (
                <button
                  key={item.id}
                  onClick={() => scrollToSection(item.id)}
                  className={`block w-full text-left py-2 text-sm font-medium transition-colors hover:text-blue-600 ${
                    currentSection === item.id ? 'text-blue-600' : 'text-gray-700'
                  }`}
                >
                  {item.label}
                </button>
              ))}
            </div>
          )}
        </div>
      </nav>

      {/* Hero Section */}
      <section id="hero" className="pt-16 min-h-screen flex items-center justify-center relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-blue-600 via-purple-600 to-blue-800"></div>
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 left-20 w-32 h-32 bg-white rounded-full"></div>
          <div className="absolute top-40 right-32 w-24 h-24 bg-white rounded-full"></div>
          <div className="absolute bottom-32 left-32 w-20 h-20 bg-white rounded-full"></div>
          <div className="absolute bottom-20 right-20 w-28 h-28 bg-white rounded-full"></div>
        </div>
        
        <div className="relative z-10 text-center text-white px-4 max-w-4xl mx-auto">
          <div className="mb-8">
            <div className="w-32 h-32 mx-auto bg-white/20 rounded-full flex items-center justify-center backdrop-blur-sm mb-6 animate-bounce">
              <ShoppingCart className="w-16 h-16 text-white" />
            </div>
          </div>
          
          <h1 className="text-6xl md:text-7xl font-bold mb-4 animate-fade-in">
            KURESEL
          </h1>
          
          <h2 className="text-2xl md:text-3xl font-light mb-8 opacity-90">
            Plateforme E-commerce Multi-Utilisateurs
          </h2>
          
          <p className="text-lg md:text-xl opacity-80 mb-12 max-w-3xl mx-auto leading-relaxed">
            Une solution complète développée en JavaFX pour la gestion d'un système e-commerce 
            avec interfaces dédiées pour clients, administrateurs et livreurs
          </p>
          
          <div className="flex justify-center space-x-8 mb-8">
            <div className="text-center animate-pulse">
              <div className="w-16 h-16 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm mb-2">
                <Code className="w-8 h-8 text-white" />
              </div>
              <span className="text-sm opacity-75">JavaFX</span>
            </div>
            <div className="text-center animate-pulse" style={{animationDelay: '0.2s'}}>
              <div className="w-16 h-16 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm mb-2">
                <Database className="w-8 h-8 text-white" />
              </div>
              <span className="text-sm opacity-75">MySQL</span>
            </div>
            <div className="text-center animate-pulse" style={{animationDelay: '0.4s'}}>
              <div className="w-16 h-16 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm mb-2">
                <Cog className="w-8 h-8 text-white" />
              </div>
              <span className="text-sm opacity-75">FXML</span>
            </div>
          </div>
          
          <Button 
            onClick={() => scrollToSection('overview')}
            className="bg-white text-blue-600 hover:bg-gray-100 px-8 py-3 text-lg font-semibold"
          >
            Découvrir le Projet
            <ChevronDown className="ml-2 h-5 w-5" />
          </Button>
        </div>
      </section>

      {/* Overview Section */}
      <section id="overview" className="py-20 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold text-gray-800 mb-4">
              Aperçu du Projet
            </h2>
            <div className="w-24 h-1 bg-blue-600 mx-auto rounded-full"></div>
          </div>
          
          <div className="grid md:grid-cols-2 gap-8 mb-16">
            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                    <Star className="w-6 h-6 text-blue-600" />
                  </div>
                  <CardTitle>Objectif Principal</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-gray-600 leading-relaxed">
                  Développer une plateforme e-commerce complète et intuitive permettant la gestion 
                  efficace des ventes en ligne avec un système multi-utilisateurs robuste.
                </p>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                    <Users className="w-6 h-6 text-green-600" />
                  </div>
                  <CardTitle>Utilisateurs Cibles</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  <Badge variant="secondary" className="bg-blue-100 text-blue-800">Clients</Badge>
                  <Badge variant="secondary" className="bg-purple-100 text-purple-800">Administrateurs</Badge>
                  <Badge variant="secondary" className="bg-orange-100 text-orange-800">Livreurs</Badge>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card className="bg-gradient-to-br from-blue-50 to-indigo-100">
            <CardHeader>
              <div className="flex items-center space-x-3">
                <div className="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center">
                  <CheckCircle className="w-6 h-6 text-indigo-600" />
                </div>
                <CardTitle>Fonctionnalités Clés</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid md:grid-cols-4 gap-4">
                {[
                  'Authentification sécurisée',
                  'Gestion des produits',
                  'Panier d\'achat',
                  'Suivi des commandes',
                  'Gestion des livraisons',
                  'Tableau de bord',
                  'Système de paiement',
                  'Interface responsive'
                ].map((feature, index) => (
                  <div key={index} className="flex items-center space-x-2">
                    <CheckCircle className="w-4 h-4 text-green-500 flex-shrink-0" />
                    <span className="text-sm text-gray-700">{feature}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <div className="flex justify-center space-x-12 mt-12">
            <div className="text-center">
              <div className="text-3xl font-bold text-blue-600 mb-1">3</div>
              <div className="text-gray-600 text-sm">Types d'utilisateurs</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-green-600 mb-1">20+</div>
              <div className="text-gray-600 text-sm">Contrôleurs</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-purple-600 mb-1">15+</div>
              <div className="text-gray-600 text-sm">Vues FXML</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-orange-600 mb-1">10+</div>
              <div className="text-gray-600 text-sm">Modèles de données</div>
            </div>
          </div>
        </div>
      </section>

      {/* Architecture Section */}
      <section id="architecture" className="py-20 px-4 bg-gray-50">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold text-gray-800 mb-4">
              Architecture Technique
            </h2>
            <div className="w-24 h-1 bg-blue-600 mx-auto rounded-full"></div>
          </div>

          <div className="relative mb-16">
            <div className="flex flex-col items-center space-y-8">
              {/* Presentation Layer */}
              <Card className="w-full max-w-md bg-gradient-to-r from-blue-500 to-blue-600 text-white hover:scale-105 transition-transform">
                <CardHeader className="text-center">
                  <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Code className="w-8 h-8 text-white" />
                  </div>
                  <CardTitle>Couche Présentation</CardTitle>
                  <CardDescription className="text-blue-100">JavaFX + FXML</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex justify-center space-x-2">
                    <Badge variant="secondary" className="bg-white/20 text-white border-white/30">UI/UX</Badge>
                    <Badge variant="secondary" className="bg-white/20 text-white border-white/30">Controllers</Badge>
                  </div>
                </CardContent>
              </Card>

              <ArrowRight className="w-8 h-8 text-purple-600 rotate-90" />

              {/* Business Logic Layer */}
              <div className="flex flex-col md:flex-row space-y-4 md:space-y-0 md:space-x-8">
                <Card className="bg-gradient-to-r from-purple-500 to-purple-600 text-white hover:scale-105 transition-transform">
                  <CardHeader className="text-center">
                    <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-3">
                      <Cog className="w-8 h-8 text-white" />
                    </div>
                    <CardTitle className="text-lg">Logique Métier</CardTitle>
                    <CardDescription className="text-purple-100">Contrôleurs Java</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex justify-center space-x-1">
                      <Badge variant="secondary" className="bg-white/20 text-white border-white/30 text-xs">Services</Badge>
                      <Badge variant="secondary" className="bg-white/20 text-white border-white/30 text-xs">Validation</Badge>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-to-r from-green-500 to-green-600 text-white hover:scale-105 transition-transform">
                  <CardHeader className="text-center">
                    <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-3">
                      <Database className="w-8 h-8 text-white" />
                    </div>
                    <CardTitle className="text-lg">Modèles de Données</CardTitle>
                    <CardDescription className="text-green-100">Classes Java</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex justify-center space-x-1">
                      <Badge variant="secondary" className="bg-white/20 text-white border-white/30 text-xs">Entities</Badge>
                      <Badge variant="secondary" className="bg-white/20 text-white border-white/30 text-xs">POJOs</Badge>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <ArrowRight className="w-8 h-8 text-orange-600 rotate-90" />

              {/* Data Access Layer */}
              <Card className="w-full max-w-md bg-gradient-to-r from-orange-500 to-red-500 text-white hover:scale-105 transition-transform">
                <CardHeader className="text-center">
                  <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Database className="w-8 h-8 text-white" />
                  </div>
                  <CardTitle>Couche Données</CardTitle>
                  <CardDescription className="text-orange-100">MySQL + JDBC</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex justify-center space-x-2">
                    <Badge variant="secondary" className="bg-white/20 text-white border-white/30">Connexions</Badge>
                    <Badge variant="secondary" className="bg-white/20 text-white border-white/30">Requêtes</Badge>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-5 gap-6">
            {[
              { icon: Code, name: 'Java', desc: 'Langage principal', color: 'red' },
              { icon: Code, name: 'JavaFX', desc: 'Interface utilisateur', color: 'blue' },
              { icon: Database, name: 'MySQL', desc: 'Base de données', color: 'green' },
              { icon: Cog, name: 'FXML', desc: 'Définition UI', color: 'purple' },
              { icon: Shield, name: 'JDBC', desc: 'Connectivité DB', color: 'yellow' }
            ].map((tech, index) => (
              <div key={index} className="text-center">
                <div className={`w-16 h-16 bg-${tech.color}-100 rounded-full flex items-center justify-center mb-3 mx-auto`}>
                  <tech.icon className={`w-8 h-8 text-${tech.color}-600`} />
                </div>
                <h4 className="font-semibold text-gray-800">{tech.name}</h4>
                <p className="text-sm text-gray-600">{tech.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Users Section */}
      <section id="users" className="py-20 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-4">
              Types d'Utilisateurs
            </h2>
            <div className="w-24 h-1 bg-gradient-to-r from-blue-600 to-purple-600 mx-auto rounded-full"></div>
            <p className="text-xl text-gray-600 mt-4">Trois rôles distincts pour une gestion optimale</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {/* Client */}
            <Card className="bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200 hover:shadow-xl transition-all hover:-translate-y-2">
              <CardHeader className="text-center">
                <div className="w-20 h-20 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center mx-auto mb-6 animate-bounce">
                  <Users className="w-10 h-10 text-white" />
                </div>
                <CardTitle className="text-2xl text-blue-800">Client</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {[
                    { icon: ShoppingCart, text: 'Navigation des produits' },
                    { icon: ShoppingCart, text: 'Gestion du panier' },
                    { icon: Shield, text: 'Processus de commande' },
                    { icon: CheckCircle, text: 'Historique des achats' },
                    { icon: Users, text: 'Gestion du profil' }
                  ].map((item, index) => (
                    <div key={index} className="flex items-center space-x-3">
                      <item.icon className="w-5 h-5 text-blue-600" />
                      <span className="text-gray-700">{item.text}</span>
                    </div>
                  ))}
                </div>
                <Badge className="mt-6 bg-blue-200 text-blue-800">Type: 1</Badge>
              </CardContent>
            </Card>

            {/* Administrateur */}
            <Card className="bg-gradient-to-br from-purple-50 to-purple-100 border-purple-200 hover:shadow-xl transition-all hover:-translate-y-2">
              <CardHeader className="text-center">
                <div className="w-20 h-20 bg-gradient-to-br from-purple-500 to-purple-600 rounded-full flex items-center justify-center mx-auto mb-6 animate-bounce" style={{animationDelay: '0.5s'}}>
                  <Shield className="w-10 h-10 text-white" />
                </div>
                <CardTitle className="text-2xl text-purple-800">Administrateur</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {[
                    { icon: Database, text: 'Gestion des produits' },
                    { icon: CheckCircle, text: 'Suivi des commandes' },
                    { icon: Users, text: 'Gestion des clients' },
                    { icon: Truck, text: 'Gestion des livreurs' },
                    { icon: Star, text: 'Rapports et statistiques' }
                  ].map((item, index) => (
                    <div key={index} className="flex items-center space-x-3">
                      <item.icon className="w-5 h-5 text-purple-600" />
                      <span className="text-gray-700">{item.text}</span>
                    </div>
                  ))}
                </div>
                <Badge className="mt-6 bg-purple-200 text-purple-800">Type: 2</Badge>
              </CardContent>
            </Card>

            {/* Livreur */}
            <Card className="bg-gradient-to-br from-orange-50 to-orange-100 border-orange-200 hover:shadow-xl transition-all hover:-translate-y-2">
              <CardHeader className="text-center">
                <div className="w-20 h-20 bg-gradient-to-br from-orange-500 to-orange-600 rounded-full flex items-center justify-center mx-auto mb-6 animate-bounce" style={{animationDelay: '1s'}}>
                  <Truck className="w-10 h-10 text-white" />
                </div>
                <CardTitle className="text-2xl text-orange-800">Livreur</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {[
                    { icon: CheckCircle, text: 'Commandes disponibles' },
                    { icon: Truck, text: 'Livraisons en cours' },
                    { icon: CheckCircle, text: 'Confirmation de livraison' },
                    { icon: Star, text: 'Suivi des revenus' },
                    { icon: Database, text: 'Historique des livraisons' }
                  ].map((item, index) => (
                    <div key={index} className="flex items-center space-x-3">
                      <item.icon className="w-5 h-5 text-orange-600" />
                      <span className="text-gray-700">{item.text}</span>
                    </div>
                  ))}
                </div>
                <Badge className="mt-6 bg-orange-200 text-orange-800">Type: 3</Badge>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 px-4 bg-gray-50">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold text-gray-800 mb-4">
              Fonctionnalités Principales
            </h2>
            <div className="w-24 h-1 bg-blue-600 mx-auto rounded-full"></div>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[
              {
                icon: Shield,
                title: 'Authentification Sécurisée',
                description: 'Système de connexion robuste avec validation des données et gestion des sessions utilisateurs.',
                color: 'blue'
              },
              {
                icon: ShoppingCart,
                title: 'Gestion E-commerce',
                description: 'Catalogue produits, panier d\'achat, processus de commande et suivi des achats.',
                color: 'green'
              },
              {
                icon: Truck,
                title: 'Système de Livraison',
                description: 'Attribution automatique des livraisons, suivi en temps réel et gestion des livreurs.',
                color: 'orange'
              },
              {
                icon: Database,
                title: 'Base de Données MySQL',
                description: 'Structure relationnelle optimisée avec requêtes sécurisées et performances élevées.',
                color: 'purple'
              },
              {
                icon: Users,
                title: 'Multi-Utilisateurs',
                description: 'Interfaces dédiées pour chaque type d\'utilisateur avec permissions spécifiques.',
                color: 'indigo'
              },
              {
                icon: Star,
                title: 'Tableaux de Bord',
                description: 'Visualisation des données, statistiques et rapports personnalisés par rôle.',
                color: 'pink'
              }
            ].map((feature, index) => (
              <Card key={index} className="hover:shadow-lg transition-all hover:-translate-y-1">
                <CardHeader>
                  <div className={`w-12 h-12 bg-${feature.color}-100 rounded-lg flex items-center justify-center mb-4`}>
                    <feature.icon className={`w-6 h-6 text-${feature.color}-600`} />
                  </div>
                  <CardTitle className="text-xl">{feature.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-gray-600">{feature.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Database Section */}
      <section id="database" className="py-20 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold text-gray-800 mb-4">
              Base de Données
            </h2>
            <div className="w-24 h-1 bg-indigo-600 mx-auto rounded-full"></div>
            <p className="text-xl text-gray-600 mt-4">Structure MySQL et modèles de données</p>
          </div>

          <div className="text-center mb-12">
            <div className="w-24 h-24 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-full flex items-center justify-center mx-auto mb-4 animate-pulse">
              <Database className="w-12 h-12 text-white" />
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-2">KURESEL Database</h3>
            <Badge variant="outline" className="text-sm">MySQL • localhost:3306</Badge>
          </div>

          <div className="grid md:grid-cols-3 gap-8 mb-12">
            {/* Core Tables */}
            <div>
              <h4 className="text-lg font-bold text-gray-800 text-center mb-6">Tables Principales</h4>
              <div className="space-y-4">
                <Card className="bg-gradient-to-br from-blue-50 to-blue-100 hover:shadow-lg transition-shadow">
                  <CardHeader className="pb-3">
                    <div className="flex items-center space-x-3">
                      <Users className="w-5 h-5 text-blue-600" />
                      <CardTitle className="text-lg text-blue-800">clients</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="text-xs space-y-1">
                      <div className="flex justify-between">
                        <span className="text-gray-600">id</span>
                        <Badge variant="secondary" className="text-xs">INT PK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">nom</span>
                        <span className="text-gray-500">VARCHAR</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">email</span>
                        <span className="text-gray-500">VARCHAR</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">type</span>
                        <Badge variant="secondary" className="text-xs">INT</Badge>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-to-br from-green-50 to-green-100 hover:shadow-lg transition-shadow">
                  <CardHeader className="pb-3">
                    <div className="flex items-center space-x-3">
                      <Database className="w-5 h-5 text-green-600" />
                      <CardTitle className="text-lg text-green-800">produits</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="text-xs space-y-1">
                      <div className="flex justify-between">
                        <span className="text-gray-600">id</span>
                        <Badge variant="secondary" className="text-xs">INT PK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">nom</span>
                        <span className="text-gray-500">VARCHAR</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">prix</span>
                        <span className="text-gray-500">DECIMAL</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">stock</span>
                        <span className="text-gray-500">INT</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>

            {/* Transaction Tables */}
            <div>
              <h4 className="text-lg font-bold text-gray-800 text-center mb-6">Tables Transactionnelles</h4>
              <div className="space-y-4">
                <Card className="bg-gradient-to-br from-purple-50 to-purple-100 hover:shadow-lg transition-shadow">
                  <CardHeader className="pb-3">
                    <div className="flex items-center space-x-3">
                      <ShoppingCart className="w-5 h-5 text-purple-600" />
                      <CardTitle className="text-lg text-purple-800">commandes</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="text-xs space-y-1">
                      <div className="flex justify-between">
                        <span className="text-gray-600">id</span>
                        <Badge variant="secondary" className="text-xs">INT PK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">client_id</span>
                        <Badge variant="outline" className="text-xs">INT FK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">date_commande</span>
                        <span className="text-gray-500">DATETIME</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">statut</span>
                        <span className="text-gray-500">VARCHAR</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-to-br from-orange-50 to-orange-100 hover:shadow-lg transition-shadow">
                  <CardHeader className="pb-3">
                    <div className="flex items-center space-x-3">
                      <CheckCircle className="w-5 h-5 text-orange-600" />
                      <CardTitle className="text-lg text-orange-800">lignes_commande</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="text-xs space-y-1">
                      <div className="flex justify-between">
                        <span className="text-gray-600">id</span>
                        <Badge variant="secondary" className="text-xs">INT PK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">commande_id</span>
                        <Badge variant="outline" className="text-xs">INT FK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">produit_id</span>
                        <Badge variant="outline" className="text-xs">INT FK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">quantite</span>
                        <span className="text-gray-500">INT</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>

            {/* Delivery Tables */}
            <div>
              <h4 className="text-lg font-bold text-gray-800 text-center mb-6">Tables Livraison</h4>
              <div className="space-y-4">
                <Card className="bg-gradient-to-br from-red-50 to-red-100 hover:shadow-lg transition-shadow">
                  <CardHeader className="pb-3">
                    <div className="flex items-center space-x-3">
                      <Truck className="w-5 h-5 text-red-600" />
                      <CardTitle className="text-lg text-red-800">livraisons</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="text-xs space-y-1">
                      <div className="flex justify-between">
                        <span className="text-gray-600">id</span>
                        <Badge variant="secondary" className="text-xs">INT PK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">commande_id</span>
                        <Badge variant="outline" className="text-xs">INT FK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">livreur_id</span>
                        <Badge variant="outline" className="text-xs">INT FK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">statut</span>
                        <span className="text-gray-500">VARCHAR</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-to-br from-indigo-50 to-indigo-100 hover:shadow-lg transition-shadow">
                  <CardHeader className="pb-3">
                    <div className="flex items-center space-x-3">
                      <Shield className="w-5 h-5 text-indigo-600" />
                      <CardTitle className="text-lg text-indigo-800">paiements</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="text-xs space-y-1">
                      <div className="flex justify-between">
                        <span className="text-gray-600">id</span>
                        <Badge variant="secondary" className="text-xs">INT PK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">commande_id</span>
                        <Badge variant="outline" className="text-xs">INT FK</Badge>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">montant</span>
                        <span className="text-gray-500">DECIMAL</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">methode</span>
                        <span className="text-gray-500">VARCHAR</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
            {[
              { name: 'Client.java', desc: 'Modèle utilisateur', color: 'blue' },
              { name: 'Produit.java', desc: 'Modèle produit', color: 'green' },
              { name: 'Commande.java', desc: 'Modèle commande', color: 'purple' },
              { name: 'Livraison.java', desc: 'Modèle livraison', color: 'red' },
              { name: 'Paiement.java', desc: 'Modèle paiement', color: 'indigo' }
            ].map((model, index) => (
              <Card key={index} className="text-center hover:shadow-lg transition-shadow">
                <CardContent className="pt-6">
                  <Code className={`w-8 h-8 text-${model.color}-600 mx-auto mb-2`} />
                  <h5 className="font-bold text-gray-800 text-sm">{model.name}</h5>
                  <p className="text-xs text-gray-600">{model.desc}</p>
                </CardContent>
              </Card>
            ))}
          </div>

          <Card className="bg-gray-900 text-white">
            <CardContent className="pt-6">
              <div className="flex items-center justify-center space-x-8 text-sm">
                <div className="flex items-center space-x-2">
                  <Database className="w-4 h-4 text-blue-400" />
                  <span>DatabaseConnection.java</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Shield className="w-4 h-4 text-green-400" />
                  <span>JDBC Driver</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-yellow-400" />
                  <span>PreparedStatements</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Cog className="w-4 h-4 text-purple-400" />
                  <span>Connection Pooling</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Conclusion Section */}
      <section id="conclusion" className="py-20 px-4 bg-gradient-to-br from-blue-600 via-purple-600 to-blue-800 text-white relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 left-20 w-32 h-32 bg-white rounded-full"></div>
          <div className="absolute top-40 right-32 w-24 h-24 bg-white rounded-full"></div>
          <div className="absolute bottom-32 left-32 w-20 h-20 bg-white rounded-full"></div>
          <div className="absolute bottom-20 right-20 w-28 h-28 bg-white rounded-full"></div>
        </div>

        <div className="max-w-7xl mx-auto relative z-10">
          <div className="text-center mb-16">
            <div className="w-20 h-20 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-6 animate-bounce">
              <Trophy className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Conclusion et Perspectives
            </h2>
            <div className="w-32 h-1 bg-white/50 mx-auto rounded-full"></div>
          </div>

          <div className="grid md:grid-cols-3 gap-8 mb-16">
            {/* Technical Achievement */}
            <Card className="bg-white/10 backdrop-blur-md border-white/20 text-white hover:bg-white/15 transition-all hover:-translate-y-2">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 bg-green-500/80 rounded-full flex items-center justify-center">
                    <Code className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle>Réalisations Techniques</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-2 text-sm opacity-90">
                  {[
                    'Architecture MVC complète',
                    'Interface JavaFX responsive',
                    'Base de données MySQL intégrée',
                    'Système multi-utilisateurs'
                  ].map((achievement, index) => (
                    <div key={index} className="flex items-center space-x-2">
                      <CheckCircle className="w-4 h-4 text-green-400 flex-shrink-0" />
                      <span>{achievement}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Functional Achievement */}
            <Card className="bg-white/10 backdrop-blur-md border-white/20 text-white hover:bg-white/15 transition-all hover:-translate-y-2">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 bg-blue-500/80 rounded-full flex items-center justify-center">
                    <Users className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle>Fonctionnalités Clés</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-2 text-sm opacity-90">
                  {[
                    'Gestion complète e-commerce',
                    'Système de livraison intégré',
                    'Tableaux de bord dynamiques',
                    'Authentification sécurisée'
                  ].map((feature, index) => (
                    <div key={index} className="flex items-center space-x-2">
                      <CheckCircle className="w-4 h-4 text-blue-400 flex-shrink-0" />
                      <span>{feature}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Impact Achievement */}
            <Card className="bg-white/10 backdrop-blur-md border-white/20 text-white hover:bg-white/15 transition-all hover:-translate-y-2">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 bg-purple-500/80 rounded-full flex items-center justify-center">
                    <Star className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle>Impact du Projet</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-2 text-sm opacity-90">
                  {[
                    'Solution e-commerce complète',
                    'Expérience utilisateur optimisée',
                    'Gestion efficace des livraisons',
                    'Évolutivité et maintenabilité'
                  ].map((impact, index) => (
                    <div key={index} className="flex items-center space-x-2">
                      <CheckCircle className="w-4 h-4 text-purple-400 flex-shrink-0" />
                      <span>{impact}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="mb-12">
            <h3 className="text-3xl font-bold text-center mb-8 flex items-center justify-center">
              <Lightbulb className="mr-3 animate-pulse" />
              Perspectives d'Évolution
            </h3>

            <div className="grid md:grid-cols-2 gap-8">
              <Card className="bg-white/10 backdrop-blur-md border-white/20 text-white">
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <Cog className="mr-3 text-yellow-400" />
                    Améliorations Techniques
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3 text-sm">
                    {[
                      { icon: ShoppingCart, text: 'Application mobile (Android/iOS)' },
                      { icon: Database, text: 'Migration vers le cloud' },
                      { icon: Shield, text: 'Sécurité renforcée (OAuth, JWT)' },
                      { icon: Cog, text: 'API REST pour intégrations' }
                    ].map((item, index) => (
                      <div key={index} className="flex items-center space-x-3">
                        <item.icon className="w-5 h-5 text-yellow-400" />
                        <span>{item.text}</span>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              <Card className="bg-white/10 backdrop-blur-md border-white/20 text-white">
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <Lightbulb className="mr-3 text-orange-400" />
                    Nouvelles Fonctionnalités
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3 text-sm">
                    {[
                      { icon: Star, text: 'Recommandations IA' },
                      { icon: Truck, text: 'Géolocalisation en temps réel' },
                      { icon: Users, text: 'Chat client-livreur' },
                      { icon: Trophy, text: 'Système de notation avancé' }
                    ].map((item, index) => (
                      <div key={index} className="flex items-center space-x-3">
                        <item.icon className="w-5 h-5 text-orange-400" />
                        <span>{item.text}</span>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>

          <Card className="bg-white/20 backdrop-blur-sm border-white/30">
            <CardContent className="pt-6 text-center">
              <h3 className="text-2xl font-bold text-white mb-3">
                KURESEL : Une Base Solide pour l'Avenir
              </h3>
              <p className="text-lg text-white/90 leading-relaxed mb-6">
                Ce projet démontre la maîtrise des technologies Java et JavaFX pour créer 
                une solution e-commerce complète, évolutive et prête pour les défis futurs du commerce électronique.
              </p>

              <div className="flex justify-center space-x-8">
                <div className="text-center">
                  <div className="text-2xl font-bold text-white">100%</div>
                  <div className="text-sm text-white/75">Fonctionnel</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-white">3</div>
                  <div className="text-sm text-white/75">Types d'utilisateurs</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-white">∞</div>
                  <div className="text-sm text-white/75">Possibilités</div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-8 px-4">
        <div className="max-w-7xl mx-auto text-center">
          <div className="flex items-center justify-center space-x-2 mb-4">
            <ShoppingCart className="h-6 w-6 text-blue-400" />
            <span className="text-xl font-bold">KURESEL</span>
          </div>
          <p className="text-gray-400 mb-4">
            Plateforme E-commerce Multi-Utilisateurs développée en JavaFX
          </p>
          <div className="flex justify-center space-x-6 text-sm text-gray-400">
            <span>© 2024 KURESEL Project</span>
            <span>•</span>
            <span>Développé avec JavaFX & MySQL</span>
            <span>•</span>
            <span>Architecture MVC</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;


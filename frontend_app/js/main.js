(function($) {

	"use strict";


	$(window).stellar({
    responsive: true,
    parallaxBackgrounds: true,
    parallaxElements: true,
    horizontalScrolling: false,
    hideDistantElements: false,
    scrollProperty: 'scroll'
  });


	var fullHeight = function() {

		$('.js-fullheight').css('height', $(window).height());
		$(window).resize(function(){
			$('.js-fullheight').css('height', $(window).height());
		});

	};
	fullHeight();

	// loader
	var loader = function() {
		setTimeout(function() { 
			if($('#ftco-loader').length > 0) {
				$('#ftco-loader').removeClass('show');
			}
		}, 1);
	};
	loader();

  var carousel = function() {
		$('.carousel-testimony').owlCarousel({
			center: true,
			loop: true,
			items:1,
			margin: 30,
			stagePadding: 0,
			nav: false,
			navText: ['<span class="ion-ios-arrow-back">', '<span class="ion-ios-arrow-forward">'],
			responsive:{
				0:{
					items: 1
				},
				600:{
					items: 2
				},
				1000:{
					items: 3
				}
			}
		});

	};
	carousel();

	$('nav .dropdown').hover(function(){
		var $this = $(this);
		// 	 timer;
		// clearTimeout(timer);
		$this.addClass('show');
		$this.find('> a').attr('aria-expanded', true);
		// $this.find('.dropdown-menu').addClass('animated-fast fadeInUp show');
		$this.find('.dropdown-menu').addClass('show');
	}, function(){
		var $this = $(this);
			// timer;
		// timer = setTimeout(function(){
			$this.removeClass('show');
			$this.find('> a').attr('aria-expanded', false);
			// $this.find('.dropdown-menu').removeClass('animated-fast fadeInUp show');
			$this.find('.dropdown-menu').removeClass('show');
		// }, 100);
	});


	$('#dropdown04').on('show.bs.dropdown', function () {
	  console.log('show');
	});

	// magnific popup
	$('.image-popup').magnificPopup({
    type: 'image',
    closeOnContentClick: true,
    closeBtnInside: false,
    fixedContentPos: true,
    mainClass: 'mfp-no-margins mfp-with-zoom', // class to remove default margin from left and right side
     gallery: {
      enabled: true,
      navigateByImgClick: true,
      preload: [0,1] // Will preload 0 - before current, and 1 after the current image
    },
    image: {
      verticalFit: true
    },
    zoom: {
      enabled: true,
      duration: 300 // don't foget to change the duration also in CSS
    }
  });

  $('.popup-youtube, .popup-vimeo, .popup-gmaps').magnificPopup({
    disableOn: 700,
    type: 'iframe',
    mainClass: 'mfp-fade',
    removalDelay: 160,
    preloader: false,

    fixedContentPos: false
  });


  var counter = function() {
		
		$('#section-counter').waypoint( function( direction ) {

			if( direction === 'down' && !$(this.element).hasClass('ftco-animated') ) {

				var comma_separator_number_step = $.animateNumber.numberStepFactories.separator(',')
				$('.number').each(function(){
					var $this = $(this),
						num = $this.data('number');
						console.log(num);
					$this.animateNumber(
					  {
					    number: num,
					    numberStep: comma_separator_number_step
					  }, 7000
					);
				});
				
			}

		} , { offset: '95%' } );

	}
	counter();

	var contentWayPoint = function() {
		var i = 0;
		$('.ftco-animate').waypoint( function( direction ) {

			if( direction === 'down' && !$(this.element).hasClass('ftco-animated') ) {
				
				i++;

				$(this.element).addClass('item-animate');
				setTimeout(function(){

					$('body .ftco-animate.item-animate').each(function(k){
						var el = $(this);
						setTimeout( function () {
							var effect = el.data('animate-effect');
							if ( effect === 'fadeIn') {
								el.addClass('fadeIn ftco-animated');
							} else if ( effect === 'fadeInLeft') {
								el.addClass('fadeInLeft ftco-animated');
							} else if ( effect === 'fadeInRight') {
								el.addClass('fadeInRight ftco-animated');
							} else {
								el.addClass('fadeInUp ftco-animated');
							}
							el.removeClass('item-animate');
						},  k * 50, 'easeInOutExpo' );
					});
					
				}, 100);
				
			}

		} , { offset: '95%' } );
	};
	contentWayPoint();

	$('.appointment_date').datepicker({
	  'format': 'm/d/yyyy',
	  'autoclose': true
	});

	$('.appointment_time').timepicker();


	$(document).ready(function() {
		updateNavbar();
	
		// Çıkış Yap butonuna basılınca çalışacak fonksiyon
		$(document).on('click', '#logoutBtn', function(e) {
			e.preventDefault();
			if(confirm("Çıkış yapmak istediğinize emin misiniz?")) {
				localStorage.removeItem("currentUser"); // Hafızayı temizle
				window.location.href = "index.html"; // Ana sayfaya at
			}
		});
	});
	
	function updateNavbar() {
		var storedUser = localStorage.getItem("currentUser");
		var navContainer = $(".navbar-nav.ml-auto"); // Menünün olduğu kapsayıcı
	
		if (storedUser) {
			var user = JSON.parse(storedUser);
			
			// 1. Mevcut Giriş/Kayıt butonlarını bul ve GİZLE (Silme, gizle)
			// Link metinlerine bakarak buluyoruz
			navContainer.find("a:contains('Giriş')").parent().hide(); 
			navContainer.find("a:contains('Kayıt')").parent().hide();
	
			// 2. Kullanıcı Adı ve Çıkış Butonunu EKLE (Eğer zaten ekli değilse)
			if ($("#userMenu").length === 0) {
				var userHtml = `
				   <li class="nav-item" id="userMenu">
					   <a href="profile.html" class="nav-link" style="color: #ffb400;">
						   <i class="fa fa-user-circle mr-1"></i> ${user.ad}
					   </a>
				   </li>
				   <li class="nav-item">
					   <a href="#" id="logoutBtn" class="nav-link">Çıkış Yap</a>
				   </li>
				`;
				// Admin ise Panele Git butonu da ekle
				if (user.rol === 'ADMIN') {
					 userHtml += `
						<li class="nav-item">
							<a href="admin.html" class="nav-link  text-white ml-2 px-3 rounded">Yönetici Paneli</a>
						</li>
					`;
				}
				
				navContainer.append(userHtml);
			}
		}
	}

	
})(jQuery);


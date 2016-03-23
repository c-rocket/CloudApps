define(['ojs/ojcore', 'knockout'],
  function(oj, ko) {
   /**
    * The view model for the main content view template
    */
    function mainContentViewModel() {
        var self = this;
        self.config = ko.observable("Configure me");
        
        
    }
   return new mainContentViewModel();
});


//simple screen navigation
function showPage(pageId) {
    //hide all screens first
    document.querySelectorAll('.content').forEach(page => {
        page.classList.add('hidden');
    });
    
    //show selected screen only
    document.getElementById(`${pageId}-page`).classList.remove('hidden');
    
    //update active nav item
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    
    if (pageId === 'dashboard' || pageId === 'sensor' || pageId === 'chatbot' || pageId === 'profile') {
        const navItems = document.querySelectorAll('.nav-item');
        if (pageId === 'dashboard') navItems[0].classList.add('active');
        if (pageId === 'chatbot') navItems[1].classList.add('active');
        if (pageId === 'sensor') navItems[2].classList.add('active');
        if (pageId === 'profile') navItems[3].classList.add('active');
    }
}

//functions for the measurements modal
function showMeasurementsModal() {
    document.getElementById('measurements-modal').classList.add('active');
}

function hideMeasurementsModal() {
    document.getElementById('measurements-modal').classList.remove('active');
}

//close modal when clicking outside content
document.getElementById('measurements-modal').addEventListener('click', function(e) {
    if (e.target === this) {
        hideMeasurementsModal();
    }
});

//functions for farm management
function showAddFarmForm() {
    document.getElementById('farm-form').classList.remove('hidden');
}

function hideAddFarmForm() {
    document.getElementById('farm-form').classList.add('hidden');
    //clear form fields
    document.getElementById('farm-name').value = '';
    document.getElementById('farm-location').value = '';
    document.getElementById('farm-size').value = '';
    document.getElementById('farm-crop').value = '';
}

function saveNewFarm() {
    const name = document.getElementById('farm-name').value;
    const location = document.getElementById('farm-location').value;
    const size = document.getElementById('farm-size').value;
    const crop = document.getElementById('farm-crop').value;
    
    if (name && location && size) {
        //successful add alert
        alert(`New farm "${name}" added successfully!`);
        hideAddFarmForm();
        
        //dropdown for selecting a farm
        const selector = document.getElementById('farm-selector');
        const newOption = document.createElement('option');
        newOption.value = `farm${selector.length + 1}`;
        newOption.textContent = name;
        selector.appendChild(newOption);
        selector.value = newOption.value;
        updateCurrentFarmInfo(name, location, size, crop);
    } else {
        alert('Please fill in all required fields');
    }
}

function updateCurrentFarmInfo(name, location, size, crop) {
    //updating the farm information to be displayed after selection
    const farmItems = document.querySelectorAll('#profile-page .profile-item');
    farmItems[0].querySelector('.profile-item-title').textContent = name;
    farmItems[0].querySelector('.profile-item-subtitle').textContent = location;
    farmItems[1].querySelector('.profile-item-subtitle').textContent = `${size} acres`;
    farmItems[2].querySelector('.profile-item-subtitle').textContent = crop;
    farmItems[3].querySelector('.profile-item-subtitle').textContent = 'Just now';
}

//handle farm selection changes
document.getElementById('farm-selector').addEventListener('change', function() {
    const selectedFarm = this.options[this.selectedIndex].text.replace(' (Current)', '');
    alert(`Loading data for ${selectedFarm}...`);
    Array.from(this.options).forEach(opt => {
        opt.text = opt.text.replace(' (Current)', '');
    });
    this.options[this.selectedIndex].text += ' (Current)';
    
    //updating the displayed farm information
    if (this.value === 'farm1') {
        updateCurrentFarmInfo('Thondwe Farm', 'Zomba, Malawi', '8', 'Maize, Beans, Soybeans');
    } else if (this.value === 'farm2') {
        updateCurrentFarmInfo('Lirangwe Fields', 'Blantyre, Malawi', '5', 'Tobacco, Maize');
    } else if (this.value === 'farm3') {
        updateCurrentFarmInfo('Zomba Plateau', 'Zomba, Malawi', '3', 'Coffee, Tea');
    }
});

//initializing view with the home screen
showPage('dashboard');

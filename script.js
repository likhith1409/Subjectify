document.addEventListener('DOMContentLoaded', function () {
    const searchInput = document.querySelector('.input');
    const subjects = document.querySelectorAll('.subject');

    searchInput.addEventListener('input', function () {
        const searchTerm = searchInput.value.toLowerCase();

        subjects.forEach(subject => {
            const subjectTitle = subject.querySelector('h2').textContent.toLowerCase();

            if (subjectTitle.includes(searchTerm)) {
                subject.style.display = 'block';
            } else {
                subject.style.display = 'none';
            }
        });
    });

    const uniqueButton1 = document.querySelector('.unique-button-1');
    const uniqueButton2 = document.querySelector('.unique-button-2');
    const uniqueButton3 = document.querySelector('.unique-button-3');

    uniqueButton1.addEventListener('click', function () {
        window.location.href = 'index.html';
    });

    uniqueButton2.addEventListener('click', function () {
        window.location.href = 'important.html';
    });

    uniqueButton3.addEventListener('click', function () {
        window.location.href = 'develop.html';
    });

    const viewPdfButtons = document.querySelectorAll('.view-pdf-btn');
    const downloadPdfButtons = document.querySelectorAll('.download-pdf-btn');
    const pdfContainer = document.getElementById('pdfContainer');
    const pdfIframe = document.querySelector('.pdf-iframe');
    const closePdfButton = document.querySelector('.close-pdf-btn');

    viewPdfButtons.forEach(button => {
        button.addEventListener('click', function () {
            const pdfPath = this.getAttribute('data-pdf');
            pdfIframe.src = pdfPath;
            pdfContainer.style.display = 'block';
        });
    });

    downloadPdfButtons.forEach(button => {
        button.addEventListener('click', function () {
            const pdfPath = this.getAttribute('data-pdf');
            window.open(pdfPath, '_blank');
        });
    });

    closePdfButton.addEventListener('click', function () {
        closePdfContainer();
    });
});

function closePdfContainer() {
    const pdfContainer = document.getElementById('pdfContainer');
    pdfContainer.style.display = 'none';
}

/**
 * SP MART — Complete Interactive Client-Side Script
 * B.Tech AI & Data Science Capstone Project
 */

document.addEventListener('DOMContentLoaded', () => {

    // 1. Resilient Image Fallback (SVG Tech Graphic)
    const placeholderSvg = "data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22400%22%20height%3D%22400%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%20400%20400%22%3E%3Crect%20width%3D%22400%22%20height%3D%22400%22%20fill%3D%22%23F5F7FB%22%2F%3E%3Ccircle%20cx%3D%22200%22%20cy%3D%22180%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23287BFF%22%20stroke-width%3D%228%22%2F%3E%3Cpath%20d%3D%22M170%20270h60M200%20230v40%22%20stroke%3D%22%23287BFF%22%20stroke-width%3D%228%22%20stroke-linecap%3D%22round%22%2F%3E%3Ctext%20x%3D%22200%22%20y%3D%22330%22%20text-anchor%3D%22middle%22%20font-family%3D%22sans-serif%22%20font-size%3D%2218%22%20font-weight%3D%22bold%22%20fill%3D%22%23172033%22%3ESP%20MART%3C%2Ftext%3E%3C%2Fsvg%3E";

    document.querySelectorAll('img').forEach(img => {
        img.addEventListener('error', () => {
            img.src = placeholderSvg;
        });
    });

    // 2. Password Toggle Visibility
    document.querySelectorAll('[data-toggle-password]').forEach(button => {
        button.addEventListener('click', () => {
            const targetId = button.getAttribute('data-toggle-password');
            const targetInput = document.getElementById(targetId);
            if (targetInput) {
                if (targetInput.type === 'password') {
                    targetInput.type = 'text';
                    button.textContent = 'Hide';
                } else {
                    targetInput.type = 'password';
                    button.textContent = 'Show';
                }
            }
        });
    });

    // 3. Form Double-Submit Protection
    document.querySelectorAll('form[data-prevent-double-submit]').forEach(form => {
        form.addEventListener('submit', () => {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = `<span>Processing...</span>`;
            }
        });
    });

    // 4. Quantity Stepper Controls
    document.querySelectorAll('.qty-btn-plus').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = btn.closest('.qty-control-wrapper')?.querySelector('.qty-input');
            if (input) {
                const max = parseInt(input.getAttribute('max') || '99', 10);
                const current = parseInt(input.value || '1', 10);
                if (current < max) {
                    input.value = current + 1;
                }
            }
        });
    });

    document.querySelectorAll('.qty-btn-minus').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = btn.closest('.qty-control-wrapper')?.querySelector('.qty-input');
            if (input) {
                const min = parseInt(input.getAttribute('min') || '1', 10);
                const current = parseInt(input.value || '1', 10);
                if (current > min) {
                    input.value = current - 1;
                }
            }
        });
    });

    // 5. Auto-Dismiss Alerts
    document.querySelectorAll('.alert-auto-dismiss').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // 6. Interactive AI Shopping Assistant
    const aiToggleBtn = document.getElementById('ai-assistant-toggle');
    const aiModal = document.getElementById('ai-assistant-modal');
    const aiCloseBtn = document.getElementById('ai-assistant-close');
    const aiForm = document.getElementById('ai-chat-form');
    const aiInput = document.getElementById('ai-chat-input');
    const aiMessages = document.getElementById('ai-chat-messages');
    const aiChipsContainer = document.getElementById('ai-chips');

    if (aiToggleBtn && aiModal) {
        aiToggleBtn.addEventListener('click', () => {
            aiModal.classList.toggle('open');
            if (aiModal.classList.contains('open') && aiInput) {
                aiInput.focus();
            }
        });

        if (aiCloseBtn) {
            aiCloseBtn.addEventListener('click', () => {
                aiModal.classList.remove('open');
            });
        }

        const appendUserMessage = (text) => {
            const div = document.createElement('div');
            div.className = 'ai-msg ai-msg-user';
            div.textContent = text;
            aiMessages.appendChild(div);
            aiMessages.scrollTop = aiMessages.scrollHeight;
        };

        const appendAssistantMessage = (reply, products, comparisonMode, productA, productB) => {
            const div = document.createElement('div');
            div.className = 'ai-msg ai-msg-assistant';

            // Format markdown text simply
            let formattedReply = reply
                .replace(/### (.*?)\n/g, '<h4 style="margin: 0.5rem 0; color: #142B4A;">$1</h4>')
                .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                .replace(/\*(.*?)\*/g, '<em>$1</em>')
                .replace(/\n/g, '<br>');

            div.innerHTML = `<div>${formattedReply}</div>`;

            // If products returned, render interactive product cards
            if (products && products.length > 0) {
                const cardContainer = document.createElement('div');
                cardContainer.style.marginTop = '0.75rem';
                cardContainer.style.display = 'flex';
                cardContainer.style.flexDirection = 'column';
                cardContainer.style.gap = '0.5rem';

                products.forEach(p => {
                    const card = document.createElement('div');
                    card.className = 'ai-card-item';
                    card.innerHTML = `
                        <img src="${p.imageUrl}" alt="${p.name}" class="ai-card-img" onerror="this.src='${placeholderSvg}'">
                        <div class="ai-card-info">
                            <strong style="color: #172033; display: block; font-size: 0.85rem;">${p.name}</strong>
                            <div style="color: #287BFF; font-weight: 700; font-size: 0.9rem;">
                                ₹${Number(p.price).toLocaleString('en-IN')}
                                ${p.discountPercentage > 0 ? `<span style="font-size: 0.7rem; color: #F04452; margin-left: 4px;">(${p.discountPercentage}% OFF)</span>` : ''}
                            </div>
                            <span style="font-size: 0.7rem; color: #687386;">⭐ ${p.rating} · ${p.inStock ? 'In Stock' : 'Out of Stock'}</span>
                        </div>
                        <a href="/products/${p.id}" class="btn btn-primary btn-sm" style="padding: 0.35rem 0.65rem; font-size: 0.75rem; text-decoration: none;">View</a>
                    `;
                    cardContainer.appendChild(card);
                });

                div.appendChild(cardContainer);
            }

            aiMessages.appendChild(div);
            aiMessages.scrollTop = aiMessages.scrollHeight;
        };

        const updateChips = (chips) => {
            if (!aiChipsContainer) return;
            aiChipsContainer.innerHTML = '';
            if (chips && chips.length > 0) {
                chips.forEach(chipText => {
                    const btn = document.createElement('button');
                    btn.type = 'button';
                    btn.className = 'ai-chip';
                    btn.textContent = chipText;
                    btn.addEventListener('click', () => {
                        sendAiQuery(chipText);
                    });
                    aiChipsContainer.appendChild(btn);
                });
            }
        };

        const sendAiQuery = async (queryText) => {
            if (!queryText || queryText.trim() === '') return;
            appendUserMessage(queryText);
            if (aiInput) aiInput.value = '';

            // Show typing indicator
            const typingDiv = document.createElement('div');
            typingDiv.className = 'ai-msg ai-msg-assistant';
            typingDiv.id = 'ai-typing-indicator';
            typingDiv.innerHTML = '<em>Thinking with SP MART catalog intelligence...</em>';
            aiMessages.appendChild(typingDiv);
            aiMessages.scrollTop = aiMessages.scrollHeight;

            try {
                const response = await fetch('/api/ai/assistant/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message: queryText })
                });

                const data = await response.json();
                typingDiv.remove();

                appendAssistantMessage(data.reply, data.products, data.comparisonMode, data.productA, data.productB);
                updateChips(data.suggestedPrompts);
            } catch (err) {
                if (typingDiv) typingDiv.remove();
                appendAssistantMessage("I'm sorry, I encountered a temporary connection issue. Please feel free to search our catalog or ask another question!", [], false, null, null);
            }
        };

        if (aiForm) {
            aiForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const q = aiInput.value.trim();
                sendAiQuery(q);
            });
        }

        // Attach event listeners to initial chips
        document.querySelectorAll('.ai-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                sendAiQuery(chip.textContent);
            });
        });
    }

    // 7. Newsletter AJAX Subscription
    const newsletterForm = document.getElementById('newsletter-form');
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const emailInput = document.getElementById('newsletter-email');
            const feedbackBox = document.getElementById('newsletter-feedback');
            if (!emailInput || !feedbackBox) return;

            const email = emailInput.value.trim();
            if (!email) return;

            try {
                const res = await fetch('/api/newsletter/subscribe', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `email=${encodeURIComponent(email)}`
                });
                const data = await res.json();
                feedbackBox.style.display = 'block';
                feedbackBox.className = data.success ? 'alert alert-success' : 'alert alert-danger';
                feedbackBox.textContent = data.message;
                if (data.success) {
                    emailInput.value = '';
                }
            } catch (err) {
                feedbackBox.style.display = 'block';
                feedbackBox.className = 'alert alert-danger';
                feedbackBox.textContent = 'Unable to subscribe at this moment. Please try again.';
            }
        });
    }

    // 8. Order Confirmation Web Audio Chime with Mute Option
    const confirmationBanner = document.getElementById('order-confirmation-banner');
    if (confirmationBanner) {
        const isMuted = localStorage.getItem('spmart_sound_muted') === 'true';
        const soundToggle = document.getElementById('sound-toggle-btn');

        const updateMuteUi = (muted) => {
            if (soundToggle) {
                soundToggle.innerHTML = muted ? '🔇 Unmute Chime' : '🔔 Mute Chime';
            }
        };
        updateMuteUi(isMuted);

        if (soundToggle) {
            soundToggle.addEventListener('click', () => {
                const currentMute = localStorage.getItem('spmart_sound_muted') === 'true';
                const newMute = !currentMute;
                localStorage.setItem('spmart_sound_muted', newMute ? 'true' : 'false');
                updateMuteUi(newMute);
                if (!newMute) {
                    playSuccessChime();
                }
            });
        }

        const playSuccessChime = () => {
            try {
                const AudioContext = window.AudioContext || window.webkitAudioContext;
                if (!AudioContext) return;
                const ctx = new AudioContext();

                // Note 1 (E5 - 659 Hz)
                const osc1 = ctx.createOscillator();
                const gain1 = ctx.createGain();
                osc1.type = 'sine';
                osc1.frequency.setValueAtTime(659.25, ctx.currentTime);
                gain1.gain.setValueAtTime(0.2, ctx.currentTime);
                gain1.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.6);
                osc1.connect(gain1);
                gain1.connect(ctx.destination);
                osc1.start(ctx.currentTime);
                osc1.stop(ctx.currentTime + 0.6);

                // Note 2 (A5 - 880 Hz)
                const osc2 = ctx.createOscillator();
                const gain2 = ctx.createGain();
                osc2.type = 'sine';
                osc2.frequency.setValueAtTime(880.00, ctx.currentTime + 0.2);
                gain2.gain.setValueAtTime(0.25, ctx.currentTime + 0.2);
                gain2.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.9);
                osc2.connect(gain2);
                gain2.connect(ctx.destination);
                osc2.start(ctx.currentTime + 0.2);
                osc2.stop(ctx.currentTime + 0.9);
            } catch (e) {
                // Audio context policy safe ignore
            }
        };

        if (!isMuted) {
            // Play chime safely
            setTimeout(playSuccessChime, 400);
        }
    }

    // 9. Progressive Web App (PWA) Service Worker & Mobile Install
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/sw.js').catch(() => {});
        });
    }

    let deferredInstallPrompt = null;
    window.addEventListener('beforeinstallprompt', (e) => {
        e.preventDefault();
        deferredInstallPrompt = e;
        const installBtns = document.querySelectorAll('.pwa-install-trigger');
        installBtns.forEach(btn => btn.style.display = 'inline-flex');
    });

    document.querySelectorAll('.pwa-install-trigger').forEach(btn => {
        btn.addEventListener('click', async () => {
            if (deferredInstallPrompt) {
                deferredInstallPrompt.prompt();
                const choiceResult = await deferredInstallPrompt.userChoice;
                if (choiceResult.outcome === 'accepted') {
                    document.querySelectorAll('.pwa-install-trigger').forEach(b => b.style.display = 'none');
                }
                deferredInstallPrompt = null;
            }
        });
    });
});

